package io.github.kosmx.emotes.main;

import com.google.gson.JsonElement;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import dev.kosmx.playerAnim.core.util.Vec3d;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayer;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Wrapper class to store an emote and create renderable texts + some utils
 */
public class EmoteHolder implements Supplier<UUID> {

    public final KeyframeAnimation emote;
    public final Text name;
    public final Text description;
    public final Text author;

    public AtomicInteger hash = null; // The emote's identifier hash //caching only
    public static UUIDMap<EmoteHolder> list = new UUIDMap<>(); // static array of all imported emotes
    //public InputKey keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey(); // assigned keybinding
    @Nullable
    public INativeImageBacketTexture nativeIcon = null;
    @Nullable
    private IIdentifier iconIdentifier = null;

    /**
     * Null if imported locally
     */
    @Nullable
    public INetworkInstance fromInstance = null;

    /**
     * Create cache from emote data
     * @param emote emote
     */
    public EmoteHolder(KeyframeAnimation emote) {
        this.emote = emote;
        this.name = EmoteInstance.instance.getDefaults().fromJson(emote.extraData.get("name"));
        this.description = EmoteInstance.instance.getDefaults().fromJson(emote.extraData.get("description"));
        this.author = EmoteInstance.instance.getDefaults().fromJson(emote.extraData.get("author"));
    }


    /**
     *
     * Emote params are stored in the data {@link KeyframeAnimation}
     *
     * @param emote       {@link KeyframeAnimation}
     * @param name        Emote name
     * @param description Emote description
     * @param author      Name of the Author
     * @param hash        hash from the serializer
     */
    @Deprecated
    public EmoteHolder(KeyframeAnimation emote, Text name, Text description, Text author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
    }

    /**
     * just clear the {@link EmoteHolder#list} before reimporting emotes
     * Does not remove server-emotes
     */
    public static void clearEmotes(){
        list.removeIf(emoteHolder -> {
            if(emoteHolder.fromInstance != null){
                return false;
            }
            if(emoteHolder.iconIdentifier != null){
                EmoteInstance.instance.getClientMethods().destroyTexture(emoteHolder.iconIdentifier);
                assert emoteHolder.nativeIcon != null;
                emoteHolder.nativeIcon.close();
            }
            return true;
        });
    }

    public IIdentifier getIconIdentifier(){
        if(iconIdentifier == null && this.emote.extraData.containsKey("iconData")){
            try {
                InputStream stream = new ByteArrayInputStream(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer((ByteBuffer) this.emote.extraData.get("iconData"))));
                assignIcon(stream);
                stream.close();
            }catch (IOException | NullPointerException e){
                e.printStackTrace();
                if(!((ClientConfig)EmoteInstance.config).neverRemoveBadIcon.get()){
                    this.emote.extraData.remove("iconData");
                }
            }
        }
        return iconIdentifier;
    }

    public void assignIcon(InputStream inputStream) {
        try {

            INativeImageBacketTexture nativeImageBackedTexture = EmoteInstance.instance.getClientMethods().readNativeImage(inputStream);
            this.iconIdentifier = EmoteInstance.instance.getDefaults().newIdentifier("icon" + this.hashCode());
            EmoteInstance.instance.getClientMethods().registerTexture(this.iconIdentifier, nativeImageBackedTexture);
            this.nativeIcon = nativeImageBackedTexture;

        } catch (Throwable var) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't open emote icon: " + var);
            this.iconIdentifier = null;
            this.nativeIcon = null;
        }
    }


    //public void setKeyBinding(InputUtil.Key key, )

    /**
     * @return Playable EmotePlayer
     */
    public KeyframeAnimation getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromUuid(UUID uuid){
        return list.get(uuid);
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

    public static boolean playEmote(KeyframeAnimation emote, IEmotePlayerEntity player){
        return playEmote(emote, player, null);
    }

    /**
     * Check if the emote can be played by the main player
     * @param emote emote to play
     * @param player who is the player
     * @param emoteHolder emote holder object
     * @return could be played
     */
    public static boolean playEmote(KeyframeAnimation emote, IEmotePlayerEntity player, @Nullable EmoteHolder emoteHolder){
        if(canPlayEmote(player)){
            return ClientEmotePlay.clientStartLocalEmote(emote);
        }else{
            return false;
        }
    }

    private static boolean canPlayEmote(IEmotePlayerEntity entity){
        if(! canRunEmote(entity)) return false;
        if(!entity.isMainPlayer()) return false;
        return ! (IEmotePlayer.isRunningEmote(entity.getEmote()) && ! entity.getEmote().isLoopStarted());
    }

    /**
     * Check if the emote can be played.
     * @param player Witch entity (player)
     * @return True if possible to play
     */
    public static boolean canRunEmote(IEmotePlayerEntity player){
        if(! EmoteInstance.instance.getClientMethods().isAbstractClientEntity(player)) return false;
        if(player.isNotStanding() && !ClientPacketManager.isRemoteTracking()) return false;
        //System.out.println(player.getPos().distanceTo(new Vec3d(player.prevX, player.prevY, player.prevZ)));
        Vec3d prevPos = player.getPrevPos();
        return ! (player.emotesGetPos().distanceTo(new Vec3d(prevPos.getX(), MathHelper.lerp(((ClientConfig)EmoteInstance.config).yRatio.get(), prevPos.getY(), player.emotesGetPos().getY()), prevPos.getZ())) > ((ClientConfig)EmoteInstance.config).stopThreshold.get());
    }

    public boolean playEmote(IEmotePlayerEntity playerEntity){
        return playEmote(this.emote, playerEntity, this);
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


    public static void handleKeyPress(InputKey key){
        if(EmoteInstance.instance != null && EmoteHolder.canRunEmote(EmoteInstance.instance.getClientMethods().getMainPlayer())){
            UUID uuid = ((ClientConfig)EmoteInstance.config).emoteKeyMap.getL(key);
            if(uuid != null){
                EmoteHolder emoteHolder = list.get(uuid);
                if(emoteHolder != null)ClientEmotePlay.clientStartLocalEmote(emoteHolder);
            }
        }
    }


    public static EmoteHolder getNonNull(@Nonnull UUID emote){
        EmoteHolder emoteHolder = list.get(emote);
        if(emoteHolder == null)return new Empty(emote);
        return emoteHolder;
    }


    public static class Empty extends EmoteHolder{

        public Empty(UUID uuid) {
            super(new KeyframeAnimation.AnimationBuilder(AnimationFormat.UNKNOWN).setName("{\"color\":\"red\",\"text\":\"INVALID\"}").setUuid(uuid).build());
        }
    }
}

