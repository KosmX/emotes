package io.github.kosmx.emotes.main;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Wrapper class to store an emote and create renderable texts + some utils
 */
public class EmoteHolder implements Supplier<UUID> {

    public final Animation emote;
    public final Component name;
    public final Component description;
    public final Component author;
    public final List<Component> folder;
    public final List<Component> bages;
    @Nullable
    public final Component fileName;

    public AtomicInteger hash = null; // The emote's identifier hash //caching only
    public static UUIDMap<EmoteHolder> list = new UUIDMap<>(); // static array of all imported emotes
    @Nullable
    private Identifier iconIdentifier = null;

    /**
     * Null if imported locally
     */
    @Nullable
    private INetworkInstance fromInstance = null;

    /**
     * Create cache from emote data
     * @param emote emote
     */
    @SuppressWarnings("unchecked")
    public EmoteHolder(Animation emote) {
        this.emote = emote;
        this.name = emote.data().get("name").map(McUtils::fromJson).orElseThrow();
        this.description = emote.data().get("description").map(McUtils::fromJson).orElse(CommonComponents.EMPTY);
        this.author = emote.data().get("author").map(McUtils::fromJson).orElse(CommonComponents.EMPTY);
        this.folder = computeFolderPath((String) emote.data().getRaw(EmoteSerializer.FOLDER_PATH_KEY));
        this.bages = computeBages((List<String>) emote.data().getRaw("bages"));

        this.fileName = emote.data().get(EmoteSerializer.FILENAME_KEY)
                .map(McUtils::fromJson)
                .orElse(null);
    }

    private static List<Component> computeFolderPath(String folderPath) {
        if (StringUtils.isBlank(folderPath)) return Collections.emptyList();
        return Arrays.stream(folderPath.split("/"))
                .map(Component::literal)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Component> computeBages(List<String> bages) {
        if (bages == null || bages.isEmpty()) return Collections.emptyList();
        List<Component> components = new ArrayList<>(bages.size());
        for (String element : bages) {
            try {
                components.add(McUtils.fromJson(element, RegistryAccess.EMPTY));
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to serialize bage!", th);
            }
        }
        return Collections.unmodifiableList(components);
    }

    /**
     * just clear the {@link EmoteHolder#list} before reimporting emotes
     * Does not remove server-emotes
     */
    public static void clearEmotes() {
        clearEmotes(null);
    }

    public static void clearEmotes(INetworkInstance networkInstance) {
        EmoteHolder.list.removeIf(emoteHolder -> {
            if (emoteHolder.fromInstance != networkInstance) return false;
            emoteHolder.closeIcon();
            return true;
        });
    }

    public @Nullable Identifier getIconIdentifier() {
        if (this.emote.data().getRaw("iconData") instanceof ByteBuffer buff && this.iconIdentifier == null) {
            registerIcon(buff);
        }
        return this.iconIdentifier;
    }

    private void registerIcon(ByteBuffer buffer) {
        RenderSystem.assertOnRenderThread();

        try (InputStream stream = new ByteArrayInputStream(MathHelper.safeGetBytesFromBuffer((buffer)))) {
            this.iconIdentifier = McUtils.newIdentifier("icon" + hashCode());

            Minecraft.getInstance().getTextureManager().register(this.iconIdentifier,
                    new DynamicTexture(this.iconIdentifier::toString, NativeImage.read(stream))
            );
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Can't open emote {} icon!", emote, th);
            /*if (!PlatformTools.getConfig().neverRemoveBadIcon.get()) {
                this.iconIdentifier = null;
                this.emote.data().remove("iconData");
            }*/
        }
    }

    private void closeIcon() {
        if (this.iconIdentifier == null) return;

        if (RenderSystem.isOnRenderThread()) {
            Minecraft.getInstance().getTextureManager().release(this.iconIdentifier);
        } else {
            Identifier iconIdentifier = this.iconIdentifier;
            Minecraft.getInstance().executeBlocking(() -> Minecraft.getInstance()
                    .getTextureManager().release(iconIdentifier)
            );
        }
        this.iconIdentifier = null;
    }

    /**
     * @return Playable EmotePlayer
     */
    public Animation getEmote() {
        return emote;
    }

    public static EmoteHolder getEmoteFromUuid(UUID uuid) {
        return list.get(uuid);
    }

    public static EmoteHolder findIfPresent(Animation animation) {
        if (animation == null) return null;

        EmoteHolder fast = getEmoteFromUuid(animation.uuid());
        if (fast != null && fast.emote != null && fast.emote.equals(animation)) {
            return fast;
        }

        for (EmoteHolder holder : EmoteHolder.list) {
            if (holder.emote != null && holder.emote.equals(animation)) {
                return holder;
            }
        }
        return null;
    }

    public static void addEmoteToList(Iterable<Animation> emotes, @Nullable INetworkInstance fromInstance) {
        for (Animation emote : emotes) addEmoteToList(emote, fromInstance);
    }

    public static EmoteHolder addEmoteToList(Animation emote, @Nullable INetworkInstance fromInstance) {
        EmoteHolder old = findIfPresent(emote);
        if (old != null) return old;

        EmoteHolder newEmote = new EmoteHolder(emote);
        newEmote.fromInstance = fromInstance;
        list.add(newEmote);
        return newEmote;
    }

    /**
     * Check if the emote can be played by the main player
     * @param emote emote to play
     * @param player who is the player
     * @return could be played
     */
    public static boolean playEmote(AbstractClientPlayer player, Animation emote) {
        return canPlayEmote(player) && ClientEmotePlay.clientStartLocalEmote(emote);
    }

    private static boolean canPlayEmote(AbstractClientPlayer entity) {
        if (!canRunEmote(entity)) return false;
        return entity.isMainAvatar();
    }

    /**
     * Check if the emote can be played.
     * @param player Witch entity (player)
     * @return True if possible to play
     */
    public static boolean canRunEmote(AbstractClientPlayer player) {
        return !(new Vec3(player.getX(), player.getY(), player.getZ()).distanceTo(

                new Vec3(player.xo, Mth.lerp(
                        PlatformTools.getConfig().yRatio.get(), player.yo, player.getY()
                ), player.zo)

        ) > PlatformTools.getConfig().stopThreshold.get());
    }

    public boolean playEmote() {
        return playEmote(ClientUtil.getClientPlayer(), this.emote);
    }

    /**
     * Hash code of the internal emote.
     * Cached.
     * @return hash
     */
    @Override
    public int hashCode() {
        if (hash == null) hash = new AtomicInteger(this.emote.hashCode());
        return hash.get();
    }

    public UUID getUuid() {
        return this.emote.uuid();
    }

    /**
     * The emote holder data may not be equal, but this is only cache. We may skip some work with this
     * @param o Emote holder
     * @return true if eq.... you know
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof EmoteHolder other && this.emote.equals(other.emote);
    }

    @Override
    public UUID get() {
        return this.emote.get();
    }

    public static void handleKeyPress(InputConstants.Key key) {
        if (EmoteHolder.canRunEmote(ClientUtil.getClientPlayer())) {
            UUID uuid = PlatformTools.getConfig().emoteKeyMap.getL(key);
            if (uuid != null) {
                EmoteHolder emoteHolder = list.get(uuid);
                if (emoteHolder != null) emoteHolder.playEmote();
            }
        }
    }

    public static EmoteHolder getNonNull(@NotNull UUID emote) {
        EmoteHolder emoteHolder = list.get(emote);
        if (emoteHolder == null) return new Empty(emote);
        return emoteHolder;
    }

    public static class Empty extends EmoteHolder {
        public Empty(UUID uuid) {
            super(new Animation(new ExtraAnimationData(
                            ExtraAnimationData.NAME_KEY, "{\"color\":\"red\",\"text\":\"INVALID\"}"
            ), 0, Animation.LoopType.PLAY_ONCE, Collections.emptyMap(), UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>()));
            emote.data().put(ExtraAnimationData.UUID_KEY, uuid);
        }
    }
}
