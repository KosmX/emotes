package io.github.kosmx.emotes.hytale;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.common.asset.FileCommonAsset;
import com.hypixel.hytale.server.core.cosmetics.EmoteAsset;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.hytale.asset.EmoteCache;
import io.github.kosmx.emotes.hytale.asset.EmotecraftEmoteAsset;
import io.github.kosmx.emotes.hytale.asset.MemoryCommonAsset;
import io.github.kosmx.emotes.hytale.bake.BlockyAnimBaker;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Publishes Emotecraft emotes to Hytale clients as native emotes.
 * <p>
 * Everything happens at runtime and reaches players who are already connected: touching the emote asset store
 * broadcasts {@code UpdateEmotes(AddOrUpdate)}, so an emote pulled from the cloud library becomes playable without a
 * restart, a pack rebuild, or a client mod. That broadcast carries metadata only — the blobs behind it are handed out
 * per client by {@code EmoteDelivery}.
 * <p>
 * None of that survives a shutdown, which is what {@link EmoteCache} is for: an emote is downloaded and baked once ever
 * rather than once per boot.
 */
public final class HytaleEmoteRegistry {
    /** Hytale validates that an emote's animation lives under {@code Characters/} and its icon under {@code Icons/Emotes/}. */
    private static final String ANIMATION_DIR = "Characters/Animations/Emotecraft/";
    private static final String ICON_DIR = "Icons/Emotes/";

    /** A 1x1 transparent PNG, used when an emote carries no icon of its own - the {@code Icon} field is mandatory. */
    private static final byte[] BLANK_ICON = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    );

    /** An emote as the client will receive it: the clip it animates from, the icon its wheel draws, and how long it runs. */
    public record Published(String id, CommonAsset animation, CommonAsset icon, int frames) {
        public List<CommonAsset> assets() {
            return List.of(this.animation, this.icon);
        }
    }

    private final String packKey;
    private final EmoteCache cache;
    private final Map<UUID, String> registered = new ConcurrentHashMap<>();
    private final Map<String, Published> published = new ConcurrentHashMap<>();

    /** Publications still in flight, so two players picking the same emote at once cause one download, not two. */
    private final Map<UUID, CompletableFuture<String>> publishing = new ConcurrentHashMap<>();

    public HytaleEmoteRegistry(EmoteCache cache) {
        this(DefaultAssetMap.DEFAULT_PACK_KEY, cache);
    }

    public HytaleEmoteRegistry(String packKey, EmoteCache cache) {
        this.packKey = packKey;
        this.cache = cache;
    }

    /**
     * Makes a library emote playable, downloading it only if neither this run nor the cache has it already.
     * <p>
     * Once an emote has been baked and registered, the emote id is the whole result — the parsed {@link Animation} is
     * of no further use, so nothing but that id is kept. A repeat pick therefore costs no download at all, which is
     * what keeps the library's download quota out of play.
     *
     * @param download invoked only on the first publication of this emote, ever
     * @return the Hytale emote id to pass to {@code /emote}
     */
    public CompletableFuture<String> publish(UUID emoteId, Supplier<CompletableFuture<Animation>> download) {
        String published = this.registered.get(emoteId);
        if (published != null) {
            return CompletableFuture.completedFuture(published);
        }

        EmoteCache.Entry cached = this.cache.get(emoteId);
        if (cached != null) {
            return CompletableFuture.completedFuture(publish(cached));
        }

        return this.publishing.computeIfAbsent(emoteId, id -> download.get()
                .thenApply(this::store)
                // Drop the in-flight entry either way: on success `registered` now answers, on failure a retry may work.
                .whenComplete((result, throwable) -> this.publishing.remove(id))
        );
    }

    /** @return what a client needs to play this emote, or null if the id is not one of ours */
    public Published published(String id) {
        return this.published.get(id);
    }

    /** @return the same, for a library emote that may not have been published at all yet */
    public Published publishedFor(UUID emoteId) {
        String id = this.registered.get(emoteId);
        return id == null ? null : this.published.get(id);
    }

    /** Every published icon. The client's emote wheel lists all of them at once, so it needs all of them at once. */
    public List<CommonAsset> icons() {
        return this.published.values().stream().map(Published::icon).toList();
    }

    /**
     * Republishes everything the cache holds. Called once the asset stores are up and before anyone is online, so the
     * blobs go out with the rest of the required assets at login instead of being pushed at whoever happens to be
     * connected.
     *
     * @return how many emotes were published
     */
    public int publishCached() {
        int published = 0;
        for (EmoteCache.Entry entry : this.cache.entries()) {
            if (publish(entry) != null) {
                published++;
            }
        }
        return published;
    }

    /**
     * Bakes an emote and publishes it from memory, without touching the cache.
     * <p>
     * This is the path for emotes that are already on disk in Emotecraft's own format — the built-in ones and anything
     * dropped in the emote directory. Caching those would save a bake and risk serving a stale clip for an emote whose
     * file changed under an unchanged UUID; only downloads are worth keeping.
     *
     * @return the Hytale emote id to pass to {@code /emote}, or null if the emote could not be converted
     */
    public String register(Animation emote) {
        UUID uuid = emote.uuid();
        String existing = this.registered.get(uuid);
        if (existing != null) {
            return existing;
        }

        String id = assetId(emote);
        try {
            BlockyAnimBaker.Baked animation = BlockyAnimBaker.bake(emote);
            return publish(uuid, id, animation.frames(),
                    new MemoryCommonAsset(ANIMATION_DIR + id + ".blockyanim", animation.json()),
                    new MemoryCommonAsset(ICON_DIR + id + ".png", icon(emote))
            );
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to publish emote {} to Hytale!", id, th);
            return null;
        }
    }

    /** Bakes a downloaded emote, writes it to the cache and publishes it from there. */
    private String store(Animation emote) {
        UUID uuid = emote.uuid();
        String existing = this.registered.get(uuid);
        if (existing != null) {
            return existing;
        }

        String id = assetId(emote);
        try {
            BlockyAnimBaker.Baked animation = BlockyAnimBaker.bake(emote);
            byte[] icon = icon(emote);

            EmoteCache.Entry entry = this.cache.put(uuid, id, animation, icon);
            if (entry != null) {
                return publish(entry);
            }

            // The cache is unwritable, so this emote comes back next boot. It should still play this boot.
            return publish(uuid, id, animation.frames(),
                    new MemoryCommonAsset(ANIMATION_DIR + id + ".blockyanim", animation.json()),
                    new MemoryCommonAsset(ICON_DIR + id + ".png", icon)
            );
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to publish emote {} to Hytale!", id, th);
            return null;
        }
    }

    /**
     * Publishes a cached emote. The blobs stay on disk until a client asks for one: {@code CommonAsset} holds them
     * weakly and re-reads through {@link FileCommonAsset}, so a cache of hundreds costs an index in memory and nothing
     * more.
     */
    private String publish(EmoteCache.Entry entry) {
        String existing = this.registered.get(entry.emote());
        if (existing != null) {
            return existing;
        }

        try {
            return publish(entry.emote(), entry.id(), entry.frames(),
                    new FileCommonAsset(this.cache.animationFile(entry), ANIMATION_DIR + entry.id() + ".blockyanim", entry.hash(), null),
                    new FileCommonAsset(this.cache.iconFile(entry), ICON_DIR + entry.id() + ".png", entry.iconHash(), null)
            );
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to publish cached emote {} to Hytale!", entry.id(), th);
            return null;
        }
    }

    private String publish(UUID emoteId, String id, int frames, CommonAsset animation, CommonAsset icon) {
        // Registering through the registry rather than through CommonAssetModule, which would also push both blobs at
        // every connected player and announce each one with a toast. Delivery is EmoteDelivery's job.
        CommonAssetRegistry.addCommonAsset(this.packKey, animation);
        CommonAssetRegistry.addCommonAsset(this.packKey, icon);

        // The EmoteAsset validators resolve both paths against that same registry, so the blobs have to land first.
        EmoteAsset asset = new EmotecraftEmoteAsset(id, "emotecraft.emote." + id, animation.getName(), icon.getName(), false);
        EmoteAsset.getAssetStore().loadAssets(this.packKey, List.of(asset));

        this.published.put(id, new Published(id, animation, icon, frames));
        this.registered.put(emoteId, id);
        return id;
    }

    private static byte[] icon(Animation emote) {
        ByteBuffer iconData = emote.data().getBinary("iconData");
        if (iconData == null) {
            return BLANK_ICON;
        }

        byte[] bytes = new byte[iconData.remaining()];
        iconData.duplicate().get(bytes);
        return bytes;
    }

    /**
     * Derives a Hytale asset id from the emote's name. Ids may only contain {@code A-Za-z0-9_}, and the first letter of
     * every underscore-separated segment must be uppercase, so the name is transliterated rather than used verbatim.
     * The emote's UUID disambiguates, since two library emotes may well share a name.
     */
    private String assetId(Animation emote) {
        StringBuilder id = new StringBuilder("Emotecraft_");
        boolean startOfSegment = true;

        for (char c : emote.getNameOrId().toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                id.append(startOfSegment ? Character.toUpperCase(c) : c);
                startOfSegment = false;
            } else if (c >= '0' && c <= '9' && !startOfSegment) {
                id.append(c);
            } else if (!startOfSegment) {
                startOfSegment = true; // collapse any run of separators into the next segment boundary
                id.append('_');
            }
        }

        if (startOfSegment && !id.isEmpty() && id.charAt(id.length() - 1) == '_') {
            id.setLength(id.length() - 1); // no trailing separator
        }

        return id + "_" + emote.uuid().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
