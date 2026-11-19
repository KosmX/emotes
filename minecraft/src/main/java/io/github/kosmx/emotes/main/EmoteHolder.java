package io.github.kosmx.emotes.main;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.library.modals.ShareScreen;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
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
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Wrapper class to store an emote and create renderable texts + some utils
 */
public class EmoteHolder implements Supplier<UUID> {
    private static final int ICON_TEXTURE_SIZE = 256;

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
    private final Supplier<Path> file = Suppliers.memoize(this::findFile);

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

    public static List<Component> computeBages(List<String> bages) {
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
        if (this.emote.data().getBinary("iconData") instanceof ByteBuffer buff && this.iconIdentifier == null) {
            registerIcon(buff);
        }
        return this.iconIdentifier;
    }

    private void registerIcon(ByteBuffer buffer) {
        RenderSystem.assertOnRenderThread();

        try (InputStream stream = new ByteArrayInputStream(MathHelper.safeGetBytesFromBuffer((buffer)))) {
            this.iconIdentifier = McUtils.newIdentifier("icon/" + hashCode());

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

    public ShareScreen.Icon icon(ScreenRectangle bounds) {
        return new ShareScreen.Icon(bounds, getIconIdentifier(), ICON_TEXTURE_SIZE);
    }

    public @Nullable Path file() {
        return this.file.get();
    }

    private @Nullable Path findFile() {
        ExtraAnimationData data = this.emote.data();
        if (!(data.getRaw(EmoteSerializer.FILENAME_KEY) instanceof String fileName)) return null;
        Path folder = InstanceService.INSTANCE.getExternalEmoteDir();
        if (data.getRaw(EmoteSerializer.FOLDER_PATH_KEY) instanceof String folderPath) folder = folder.resolve(folderPath);
        Path file = folder.resolve(fileName);
        return Files.isRegularFile(file) ? file : null; // One from emotes/server misses: its folder path starts there
    }

    public @Nullable URI page() {
        return EmoteLibraryClient.isLibraryEmote(getUuid()) ? EmoteLibrary.emotePage(getUuid()) : null;
    }

    /**
     * @return Playable EmotePlayer
     */
    public Animation getEmote() {
        return emote;
    }

    public static boolean hasSound(Animation emote) {
        return emote.data().getRaw(SongPacket.OPUS_KEY) instanceof OpusSound sound && !sound.failed();
    }

    public static Animation withoutSound(Animation emote) {
        ExtraAnimationData data = emote.data().copy();
        data.data().remove(SongPacket.OPUS_KEY);
        data.data().remove(SongPacket.NBS_KEY); // Clients too old for Opus still play the .nbs
        return new Animation(data, emote.length(), emote.loopType(), emote.boneAnimations(), emote.keyFrames(), emote.bones(), emote.parents());
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

    public static void removeEmotesFromList(List<UUID> removeEmoteIds, @Nullable INetworkInstance fromInstance) {
        list.removeIf(emoteHolder ->
                emoteHolder.fromInstance == fromInstance && removeEmoteIds.contains(emoteHolder.get())
        );
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
        if (!EmoteHolder.canRunEmote(ClientUtil.getClientPlayer())) return;

        EmoteHolder emoteHolder = PlatformTools.getConfig().keyBinds.get(key);
        if (emoteHolder != null) emoteHolder.playEmote();
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

    public final static class EmoteHolderEntry extends EmoteListWidget.EmoteLikeEntry {
        private static final int ICON_SIZE = 32;

        public final EmoteHolder holder;

        public EmoteHolderEntry(EmoteListWidget widget, @NotNull EmoteHolder holder) {
            widget.super(holder.name, holder.description, holder.author, holder.bages, hasSound(holder.emote), holder.page());
            this.holder = holder;
        }

        @Override
        protected @Nullable Path localPath() {
            return this.holder.file();
        }

        @Override
        protected ShareScreen.Icon icon() {
            return this.holder.icon(new ScreenRectangle(getContentX(), getContentY(), ICON_SIZE, ICON_SIZE));
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.extractAdditionalContent(graphics, mouseX, mouseY, hovered, tickDelta);

            Identifier texture = this.holder.getIconIdentifier();
            if (texture != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getContentX(), getContentY(), 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
            }
        }

        @Override
        public CompletableFuture<Animation> getEmote() {
            return CompletableFuture.completedFuture(this.holder.emote);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EmoteHolderEntry entry && this.holder.equals(entry.holder);
        }

        @Override
        public int hashCode() {
            return this.holder.hashCode();
        }

        @Override
        public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
            if (matcher.test(this)) results.accept(this);
        }

        @Override
        public UUID getUuid() {
            return this.holder.get();
        }
    }
}
