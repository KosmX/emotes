package io.github.kosmx.emotes.hytale.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.hytale.bake.BlockyAnimBaker;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Keeps every baked emote on disk, so the download that produced it is paid for once and not once per restart.
 * <p>
 * Nothing Hytale registers at runtime survives a shutdown, and re-fetching an emote body is the one operation the cloud
 * library enforces a quota on. Baking is deterministic, so the pair of blobs a publication needs — the {@code
 * .blockyanim} and the icon — is written out beside an index of {@code emote UUID -> asset id}, and a later run
 * republishes straight from there without the library ever being asked.
 * <p>
 * The index carries {@link BlockyAnimBaker#VERSION}: the retargeting constants are still unverified, so the first
 * calibration will invalidate every clip written before it. A mismatched stamp empties the whole directory rather than
 * serving poses baked against different axes.
 */
public final class EmoteCache {
    private static final String INDEX = "index.json";
    private static final String ANIMATION_EXTENSION = ".blockyanim";
    private static final String ICON_EXTENSION = ".png";

    private final Path directory;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    /**
     * @param id     the Hytale asset id the emote was published under, and the name of both files
     * @param hash   hash of the animation blob, kept so the blob itself can stay on disk until a client asks for it
     * @param frames the clip's length, which nothing else on this side of a restart can recover
     */
    public record Entry(UUID emote, String id, String hash, String iconHash, int frames) {
    }

    public EmoteCache(Path directory) {
        this.directory = directory;

        try {
            load();
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to read the Hytale emote cache, starting empty!", th);
            this.entries.clear();
        }
    }

    public Entry get(UUID emote) {
        return this.entries.get(emote);
    }

    public Collection<Entry> entries() {
        return this.entries.values();
    }

    public Path animationFile(Entry entry) {
        return this.directory.resolve(entry.id() + ANIMATION_EXTENSION);
    }

    public Path iconFile(Entry entry) {
        return this.directory.resolve(entry.id() + ICON_EXTENSION);
    }

    /**
     * Writes a freshly baked emote out.
     *
     * @return the stored entry, or null if it could not be written — in which case the caller is expected to publish
     *         from memory anyway, since a broken cache should cost speed rather than the emote itself
     */
    public Entry put(UUID emote, String id, BlockyAnimBaker.Baked animation, byte[] icon) {
        Entry entry = new Entry(emote, id, CommonAsset.hash(animation.json()), CommonAsset.hash(icon), animation.frames());

        try {
            Files.write(animationFile(entry), animation.json());
            Files.write(iconFile(entry), icon);

            this.entries.put(emote, entry);
            save();

            return entry;
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to cache emote {}!", id, e);
            return null;
        }
    }

    private void load() throws IOException {
        Files.createDirectories(this.directory);

        Path index = this.directory.resolve(INDEX);
        if (!Files.exists(index)) {
            return;
        }

        JsonObject root;
        try (Reader reader = Files.newBufferedReader(index, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Throwable th) {
            // Without the index the blobs beside it cannot be addressed at all, so they go with it.
            CommonData.LOGGER.warn("The Hytale emote cache index is unreadable, dropping the cache!", th);
            clear();
            return;
        }

        if (!root.has("bakeVersion") || root.get("bakeVersion").getAsInt() != BlockyAnimBaker.VERSION) {
            CommonData.LOGGER.info("The Hytale emote cache was baked by another version, dropping it.");
            clear();
            return;
        }

        for (Map.Entry<String, JsonElement> emote : root.getAsJsonObject("emotes").entrySet()) {
            JsonObject value = emote.getValue().getAsJsonObject();
            Entry entry = new Entry(
                    UUID.fromString(emote.getKey()), value.get("id").getAsString(),
                    value.get("hash").getAsString(), value.get("iconHash").getAsString(),
                    value.get("frames").getAsInt()
            );

            // An index entry without its blobs is worse than no entry at all: it would be published and then fail to
            // resolve on the client, so a half-deleted directory simply costs a download.
            if (Files.exists(animationFile(entry)) && Files.exists(iconFile(entry))) {
                this.entries.put(entry.emote(), entry);
            }
        }
    }

    private void save() throws IOException {
        JsonObject emotes = new JsonObject();
        for (Entry entry : this.entries.values()) {
            JsonObject value = new JsonObject();
            value.addProperty("id", entry.id());
            value.addProperty("hash", entry.hash());
            value.addProperty("iconHash", entry.iconHash());
            value.addProperty("frames", entry.frames());
            emotes.add(entry.emote().toString(), value);
        }

        JsonObject root = new JsonObject();
        root.addProperty("bakeVersion", BlockyAnimBaker.VERSION);
        root.add("emotes", emotes);

        // Through a temporary file: an index truncated by a crash mid-write would orphan every blob beside it.
        Path temporary = this.directory.resolve(INDEX + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            writer.write(root.toString());
        }
        Files.move(temporary, this.directory.resolve(INDEX), StandardCopyOption.REPLACE_EXISTING);
    }

    private void clear() throws IOException {
        this.entries.clear();

        try (Stream<Path> files = Files.list(this.directory)) {
            for (Path file : files.toList()) {
                Files.deleteIfExists(file);
            }
        }
    }
}
