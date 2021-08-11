package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.tools.Vec3d;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayer;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Class to store an emote and create renderable texts
 */
public class EmoteHolder {
    public final EmoteData emote;
    public final Text name;
    public final Text description;
    public final Text author;

    public AtomicInteger hash = null; // The emote's identifier hash //caching only
    public static Collection<EmoteHolder> list = new HashSet<>(); // static array of all imported emotes
    public InputKey keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey(); // assigned keybinding
    @Nullable
    public INativeImageBacketTexture nativeIcon = null;
    @Nullable
    private IIdentifier iconIdentifier = null;

    public boolean isFromGeckoLib = false;

    public boolean isQuarkEmote = false;

    /**
     * Create cache from emote data
     * @param emote emote
     */
    public EmoteHolder(EmoteData emote) {
        this.emote = emote;
        this.name = EmoteInstance.instance.getDefaults().fromJson(emote.name);
        this.description = EmoteInstance.instance.getDefaults().fromJson(emote.description);
        this.author = EmoteInstance.instance.getDefaults().fromJson(emote.author);
    }

    /**
     *
     * Emote params are stored in the data {@link EmoteData}
     *
     * @param emote       {@link EmoteData}
     * @param name        Emote name
     * @param description Emote decription
     * @param author      Name of the Author
     * @param hash        hash from the serializer
     */
    @Deprecated
    public EmoteHolder(EmoteData emote, Text name, Text description, Text author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
    }

    /**
     * Bind keys to emotes from config class
     * @param config config object
     */
    public static void bindKeys(ClientConfig config){
        config.emotesWithKey = new ArrayList<>();
        config.emotesWithHash = new ArrayList<>();
        for(EmoteHolder emote : list){
            if(! emote.keyBinding.equals(EmoteInstance.instance.getDefaults().getUnknownKey())){
                config.emotesWithKey.add(emote);
                config.emotesWithHash.add(new Pair<>(emote.hashCode(), emote.keyBinding.getTranslationKey()));
            }
        }
        config.fastMenuHash = new int[8];
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                config.fastMenuHash[i] = config.fastMenuEmotes[i].hashCode();
            }
        }
    }


    /**
     * just clear the {@link EmoteHolder#list} before reimporting emotes
     */
    public static void clearEmotes(){
        for(EmoteHolder emoteHolder : list){
            if(emoteHolder.iconIdentifier != null){
                EmoteInstance.instance.getClientMethods().destroyTexture(emoteHolder.iconIdentifier);
                assert emoteHolder.nativeIcon != null;
                emoteHolder.nativeIcon.close();
            }
        }
        list = new HashSet<>();
    }

    public IIdentifier getIconIdentifier(){
        if(iconIdentifier == null && this.emote.iconData != null){
            try {
                InputStream stream = new ByteArrayInputStream(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer(this.emote.iconData)));
                assignIcon(stream);
                stream.close();
            }catch (IOException | NullPointerException e){
                e.printStackTrace();
                if(!((ClientConfig)EmoteInstance.config).neverRemoveBadIcon.get()){
                    this.emote.iconData = null;
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
    public EmoteData getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromHash(int hash){
        for(EmoteHolder emote : list){
            if(emote.hashCode() == hash){
                return emote;
            }
        }
        return null;
    }

    public static void addEmoteToList(List<EmoteData> emotes){
        for(EmoteData emote : emotes){
            EmoteHolder.list.add(new EmoteHolder(emote));
        }
    }

    @Deprecated
    public static void addEmoteToList(EmoteHolder hold){
        list.add(hold);
    }

    public static boolean playEmote(EmoteData emote, IEmotePlayerEntity player){
        return playEmote(emote, player, null);
    }

    /**
     * Check if the emote can be played by the main player
     * @param emote emote to play
     * @param player who is the player
     * @param emoteHolder emote holder object
     * @return could be played
     */
    public static boolean playEmote(EmoteData emote, IEmotePlayerEntity player, @Nullable EmoteHolder emoteHolder){
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
        if(player.isNotStanding()) return false;
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
}

