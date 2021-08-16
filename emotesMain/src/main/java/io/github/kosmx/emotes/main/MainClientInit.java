package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Initializing client and other load stuff...
 *
 */
public class MainClientInit {

    public static void init(){
        loadEmotes();//:D

        ClientPacketManager.init(); //initialize proxy service
    }


    public static void loadEmotes(){
        EmoteHolder.clearEmotes();

        serializeInternalJson("waving");
        serializeInternalJson("clap");
        serializeInternalJson("crying");
        serializeInternalJson("point");
        serializeInternalJson("here");
        serializeInternalJson("palm");
        serializeInternalJson("backflip");
        serializeInternalJson("roblox_potion_dance");
        serializeInternalJson("kazotsky_kick");


        if(! EmoteInstance.instance.getExternalEmoteDir().isDirectory()) EmoteInstance.instance.getExternalEmoteDir().mkdirs();
        if (!EmoteInstance.config.loadEmotesServerSide.get()) {
            List<EmoteData> emotes = new LinkedList<>();
            EmoteSerializer.serializeEmotes(emotes, EmoteInstance.instance.getExternalEmoteDir());
            EmoteHolder.addEmoteToList(emotes);
        }
        ((ClientConfig)EmoteInstance.config).assignEmotes();
    }

    private static void serializeInternalJson(String name){
        if(!((ClientConfig)EmoteInstance.config).loadBuiltinEmotes.get()){
            return;
        }
        try {
            InputStream stream = MainClientInit.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".json");
            List<EmoteData> emotes = UniversalEmoteSerializer.readData(stream, null, "json");
            EmoteData emote = emotes.get(0);
            emote.isBuiltin = true;
            InputStream iconStream = MainClientInit.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".png");
            if(iconStream != null) {
                emote.iconData = MathHelper.readFromIStream(iconStream);
                iconStream.close();
            }
            EmoteHolder.addEmoteToList(emotes);
        }catch (EmoteSerializerException | IOException e){
            e.printStackTrace();
        }
    }

    /**
     * play the test emote
     */
    public static void playDebugEmote(){
        EmoteInstance.instance.getLogger().log(Level.INFO, "Playing debug emote");
        Path location = null;
        for(EmoteFormat source:EmoteFormat.values()){
            location = EmoteInstance.instance.getGameDirectory().resolve("emote." + source.getExtension());
            if(location.toFile().isFile()){
                break;
            }
        }
        if(location == null)return;
        try{
            InputStream reader = Files.newInputStream(location);
            EmoteHolder emoteHolder = new EmoteHolder(UniversalEmoteSerializer.readData(reader, location.getFileName().toString()).get(0));
            reader.close();
            if(EmoteInstance.instance.getClientMethods().getMainPlayer() != null){
                emoteHolder.playEmote(EmoteInstance.instance.getClientMethods().getMainPlayer());
            }
        }catch(Exception e){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Error while importing debug emote.", true);
            EmoteInstance.instance.getLogger().log(Level.INFO, e.getMessage());
            e.printStackTrace();
        }
    }
}
