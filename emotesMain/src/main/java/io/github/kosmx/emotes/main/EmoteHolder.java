package io.github.kosmx.emotes.main;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.common.tools.Vec3d;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayer;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.main.config.Serializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.quarktool.QuarkReader;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EmoteHolder {
    public final EmoteData emote;
    public final Text name;
    public final Text description;
    public final Text author;
    public final int hash; // The emote's identifier hash
    public static List<EmoteHolder> list = new ArrayList<>(); // static array of all imported emotes
    public InputKey keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey(); // assigned keybinding
    @Nullable
    public INativeImageBacketTexture nativeIcon = null;
    @Nullable
    private IIdentifier iconIdentifier = null;
    @Nullable
    public Object iconName = null; //Icon name

    public boolean isFromGeckoLib = false;

    /**
     * was it imported by {@link QuarkReader}
     */
    public boolean isQuarkEmote = false;

    public EmoteHolder setQuarkEmote(boolean bl){
        this.isQuarkEmote = bl;
        return this;
    }

    /**
     * @param emote       {@link EmoteData}
     * @param name        Emote name
     * @param description Emote decription
     * @param author      Name of the Author
     * @param hash        hash from the serializer
     */
    public EmoteHolder(EmoteData emote, Text name, Text description, Text author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hash = hash;
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
                config.emotesWithHash.add(new Pair<>(emote.hash, emote.keyBinding.getTranslationKey()));
            }
        }
        config.fastMenuHash = new int[8];
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                config.fastMenuHash[i] = config.fastMenuEmotes[i].hash;
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
        list = new ArrayList<>();
    }

    /**
     *
     * @param path try to import emote icon
     */
    public void bindIcon(Object path){
        if(path instanceof String || path instanceof File) this.iconName = path;
        else EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't use " + path.getClass() + " as file");
    }

    public void assignIcon(File file){
        if(file.isFile()){
            try{
                assignIcon(new FileInputStream(file));
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public void assignIcon(String str){
        assignIcon(EmoteHolder.class.getResourceAsStream(str));
    }

    public IIdentifier getIconIdentifier(){
        if(iconIdentifier == null && this.iconName != null){
            if(this.iconName instanceof String) assignIcon((String) this.iconName);
            else if(this.iconName instanceof File) assignIcon((File) this.iconName);
        }
        return iconIdentifier;
    }

    public void assignIcon(InputStream inputStream) {
        try {

            try {
                INativeImageBacketTexture nativeImageBackedTexture = EmoteInstance.instance.getClientMethods().readNativeImage(inputStream);
                this.iconIdentifier = EmoteInstance.instance.getDefaults().newIdentifier("icon" + this.hash);
                EmoteInstance.instance.getClientMethods().registerTexture(this.iconIdentifier, nativeImageBackedTexture);
                this.nativeIcon = nativeImageBackedTexture;
            } finally {
                try {
                    inputStream.close();
                } catch (Throwable ignore) {
                }
            }
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
            if(emote.hash == hash){
                return emote;
            }
        }
        return null;
    }

    /**
     * Deserialize helper. do not modify anything
     * @param json json file
     * @return EmoteList
     * @throws JsonParseException can be an incorrect file...
     */
    public static List<EmoteHolder> deserializeJson(BufferedReader json) throws JsonParseException{     //throws BowlingBall XD
        return Serializer.serializer.fromJson(json, new TypeToken<List<EmoteHolder>>(){}.getType());
    }

    public static void addEmoteToList(BufferedReader json) throws JsonParseException{
        list.addAll(deserializeJson(json));
    }

    public static void addEmoteToList(EmoteHolder hold){
        list.add(hold);
    }
    public static void addEmoteToList(List<EmoteHolder> hold){
        list.addAll(hold);
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
}

