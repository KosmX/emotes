package io.github.kosmx.emotes.main;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import dev.kosmx.playerAnim.core.util.Vec3d;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.PlayingAnimationData;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.Pose;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Wrapper class to store an emote and create renderable texts + some utils
 */
public class EmoteHolder implements Supplier<UUID> {

    public final KeyframeAnimation emote;
    public final Component name;
    public final Component description;
    public final Component author;
    public final List<Component> folder;
    public final List<Component> bages;

    public AtomicInteger hash = null; // The emote's identifier hash //caching only
    public static UUIDMap<EmoteHolder> list = new UUIDMap<>(); // static array of all imported emotes
    @Nullable
    private ResourceLocation iconIdentifier = null;

    /**
     * Null if imported locally
     */
    @Nullable
    public INetworkInstance fromInstance = null;

    /**
     * Create cache from emote data
     * @param emote emote
     */
    @SuppressWarnings("unchecked")
    public EmoteHolder(KeyframeAnimation emote) {
        this.emote = emote;
        this.name = McUtils.fromJson(emote.extraData.get("name"), RegistryAccess.EMPTY);
        this.description = McUtils.fromJson(emote.extraData.get("description"), RegistryAccess.EMPTY);
        this.author = McUtils.fromJson(emote.extraData.get("author"), RegistryAccess.EMPTY);
        this.folder = computeFolderPath((String) emote.extraData.get(EmoteSerializer.FOLDER_PATH_KEY));
        this.bages = computeBages((List<String>) emote.extraData.get("bages"));
    }

    private static List<Component> computeFolderPath(String folderPath) {
        if (StringUtils.isBlank(folderPath)) return Collections.emptyList();
        return Arrays.stream(folderPath.split("/"))
                .map(Component::literal)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Component> computeBages(List<String> bages) {
        if (bages == null) return Collections.emptyList();
        List<Component> components = new ArrayList<>(bages.size());
        for (String element : bages) {
            try {
                components.add(McUtils.fromJson(element, RegistryAccess.EMPTY));
            } catch (Throwable th) {
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to serialize bage!", th);
            }
        }
        return Collections.unmodifiableList(components);
    }

    /**
     * just clear the {@link EmoteHolder#list} before reimporting emotes
     * Does not remove server-emotes
     */
    public static void clearEmotes() {
        EmoteHolder.list.removeIf(emoteHolder -> {
            if (emoteHolder.fromInstance != null) return false;
            emoteHolder.closeIcon();
            return true;
        });
    }

    public @Nullable ResourceLocation getIconIdentifier() {
        if (this.emote.extraData.get("iconData") instanceof ByteBuffer buff && this.iconIdentifier == null) {
            registerIcon(buff);
        }
        return this.iconIdentifier;
    }

    private void registerIcon(ByteBuffer buffer) {
        RenderSystem.assertOnRenderThread();

        try (InputStream stream = new ByteArrayInputStream(AbstractNetworkInstance.safeGetBytesFromBuffer((buffer)))) {
            this.iconIdentifier = McUtils.newIdentifier("icon" + hashCode());

            Minecraft.getInstance().getTextureManager().register(this.iconIdentifier,
                    new DynamicTexture(this.iconIdentifier::toString, NativeImage.read(stream))
            );
        } catch (Throwable th) {
            LoggerService.INSTANCE.log(Level.WARNING, "Can't open emote icon!", th);
            this.iconIdentifier = null;

            if (!PlatformTools.getConfig().neverRemoveBadIcon.get()) {
                this.emote.extraData.remove("iconData");
            }
        }
    }

    private void closeIcon() {
        if (this.iconIdentifier == null) return;

        if (RenderSystem.isOnRenderThread()) {
            Minecraft.getInstance().getTextureManager().release(this.iconIdentifier);
        } else {
            ResourceLocation iconIdentifier = this.iconIdentifier;
            Minecraft.getInstance().executeBlocking(() -> Minecraft.getInstance()
                    .getTextureManager().release(iconIdentifier)
            );
        }
        this.iconIdentifier = null;
    }

    /**
     * @return Playable EmotePlayer
     */
    public KeyframeAnimation getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromUuid(UUID uuid){
        return list.get(uuid);
    }

    public static @Nullable EmoteHolder getEmoteFromAnimation(KeyframeAnimation animation) {
        if (animation == null) {
            return null;
        }

        EmoteHolder fast = getEmoteFromUuid(animation.getUuid());
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

    public static void addEmoteToList(Iterable<KeyframeAnimation> emotes){
        for(KeyframeAnimation emote : emotes){
            EmoteHolder.list.add(new EmoteHolder(emote));
        }
    }

    public static EmoteHolder addEmoteToList(KeyframeAnimation emote){
        EmoteHolder newEmote = new EmoteHolder(emote);
        EmoteHolder old = newEmote.findIfPresent();
        if(old == null){
            list.add(newEmote);
            return newEmote;
        }
        else {
            return old;
        }
    }

    EmoteHolder findIfPresent()
    {
        if (list.contains(this)) {
            for (EmoteHolder obj : list) {
                if (obj.equals(this))
                    return obj;
            }
        }
        return null;
    }

    @Deprecated
    public static void addEmoteToList(EmoteHolder hold){
        list.add(hold);
    }

    /**
     * Check if the emote can be played by the main player
     * @param emote emote to play
     * @param player who is the player
     * @param tick first tick
     * @return could be played
     */
    public static boolean playEmote(KeyframeAnimation emote, AbstractClientPlayer player, int tick, boolean offsetTime) {
        if(canPlayEmote(player)){
            return ClientEmotePlay.clientStartLocalEmote(new PlayingAnimationData(emote, tick, offsetTime, false));
        }else{
            return false;
        }
    }

    private static boolean canPlayEmote(AbstractClientPlayer entity){
        if(! canRunEmote(entity)) return false;
        if(!entity.isMainPlayer()) return false;
        return ! (EmotePlayer.isRunningEmote(entity.emotecraft$getEmote()) && ! entity.emotecraft$getEmote().isLoopStarted());
    }

    /**
     * Check if the emote can be played.
     * @param player Witch entity (player)
     * @return True if possible to play
     */
    public static boolean canRunEmote(AbstractClientPlayer player){
        if(!player.hasPose(Pose.STANDING) && !ClientPacketManager.isRemoteTracking()) return false;
        //System.out.println(player.getPos().distanceTo(new Vec3d(player.prevX, player.prevY, player.prevZ)));
        return ! (new Vec3d(player.getX(), player.getY(), player.getZ()).distanceTo(new Vec3d(player.xo, MathHelper.lerp(PlatformTools.getConfig().yRatio.get(), player.yo, player.getY()), player.zo)) > PlatformTools.getConfig().stopThreshold.get());
    }

    public boolean playEmote(AbstractClientPlayer playerEntity) {
        return playEmote(playerEntity, 0);
    }

    public boolean playEmote(AbstractClientPlayer playerEntity, int tick) {
        return playEmote(playerEntity, tick, this.emote.isInfinite);
    }

    public boolean playEmote(AbstractClientPlayer playerEntity, int tick, boolean offsetTime) {
        return playEmote(this.emote, playerEntity, tick, offsetTime);
    }

    /**
     * Hash code of the internal emote.
     * Cached.
     * @return hash
     */
    @Override
    public int hashCode() {
        if(hash == null)
            hash = new AtomicInteger(this.emote.hashCode());
        return hash.get();
    }

    public UUID getUuid(){
        return this.emote.getUuid();
    }
    /**
     * The emote holder data may not be equal, but this is only cache. We may skip some work with this
     * @param o Emote holder
     * @return true if eq.... you know
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof EmoteHolder){
            return (this.emote.equals(((EmoteHolder)o).emote));
        }
        return false;
    }

    @Override
    public UUID get() {
        return this.emote.get();
    }


    public static void handleKeyPress(InputConstants.Key key){
        if(EmoteHolder.canRunEmote(PlatformTools.getMainPlayer())){
            UUID uuid = PlatformTools.getConfig().emoteKeyMap.getL(key);
            if(uuid != null){
                EmoteHolder emoteHolder = list.get(uuid);
                if(emoteHolder != null)ClientEmotePlay.clientStartLocalEmote(
                        new PlayingAnimationData(emoteHolder.getEmote())
                );
            }
        }
    }


    public static EmoteHolder getNonNull(@NotNull UUID emote) {
        EmoteHolder emoteHolder = list.get(emote);
        if (emoteHolder == null) return new Empty(emote);
        return emoteHolder;
    }

    @SuppressWarnings("deprecation")
    public static class Empty extends EmoteHolder {
        public Empty(UUID uuid) {
            super(new KeyframeAnimation.AnimationBuilder(AnimationFormat.UNKNOWN).setName("{\"color\":\"red\",\"text\":\"INVALID\"}").setUuid(uuid).build());
        }
    }
}
