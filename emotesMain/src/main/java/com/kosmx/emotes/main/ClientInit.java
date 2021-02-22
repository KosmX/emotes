package com.kosmx.emotes.main;

import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.common.opennbs.NBSFileUtils;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.main.quarktool.QuarkReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Initializing client and other load stuff...
 *
 */
public class ClientInit {

    public static void init(){

    }
    public static void loadConfig(){

    }


    public static void loadEmotes(){
        EmoteHolder.clearEmotes();

        serializeInternalEmotes("waving");
        serializeInternalEmotes("clap");
        serializeInternalEmotes("crying");
        serializeInternalEmotes("point");
        serializeInternalEmotes("here");
        serializeInternalEmotes("palm");
        serializeInternalEmotes("backflip");
        serializeInternalEmotes("roblox_potion_dance");
        serializeInternalEmotes("kazotsky_kick");


        if(! EmoteInstance.instance.getExternalEmoteDir().isDirectory()) EmoteInstance.instance.getExternalEmoteDir().mkdirs();
        serializeExternalEmotes();

        ((ClientConfig)EmoteInstance.config).assignEmotes();
    }

    private static void serializeInternalEmotes(String name){
        if(((ClientConfig)EmoteInstance.config).loadBuiltinEmotes){
            return;
        }
        InputStream stream = ClientInit.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        List<EmoteHolder> emoteHolders = EmoteHolder.deserializeJson(reader); //Serializer.serializer.fromJson(reader, EmoteHolder.class);
        EmoteHolder.addEmoteToList(emoteHolders);
        emoteHolders.get(0).bindIcon(("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".png"));
    }


    private static void serializeExternalEmotes(){
        File externalEmotes = EmoteInstance.instance.getExternalEmoteDir();
        for(File file : Objects.requireNonNull(EmoteInstance.instance.getExternalEmoteDir().listFiles((dir, name)->name.endsWith(".json")))){
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                List<EmoteHolder> emotes = EmoteHolder.deserializeJson(reader);
                EmoteHolder.addEmoteToList(emotes);
                reader.close();
                File icon = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".png").toFile();
                if(icon.isFile() && emotes.size() == 1) emotes.get(0).bindIcon(icon);
                File song = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".nbs").toFile();
                if(song.isFile() && emotes.size() == 1){
                    DataInputStream bis = new DataInputStream(new FileInputStream(song));
                    try {
                        emotes.get(0).getEmote().song = NBSFileUtils.read(bis);
                    }
                    catch (IOException exception){
                        EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while reading song: " + exception.getMessage(), true);
                        if(EmoteInstance.config.showDebug) exception.printStackTrace();
                    }
                    bis.close(); //I almost forgot this
                }
            }catch(Exception e){
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while importing external emote: " + file.getName() + ".", true);
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage());
                if(EmoteInstance.config.showDebug)e.printStackTrace();
            }
        }

        if(((ClientConfig)EmoteInstance.config).enableQuark){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Quark importer is active", true);
            initQuarkEmotes(externalEmotes);
        }
    }

    private static void initQuarkEmotes(File externalEmotes){
        for(File file : Objects.requireNonNull(externalEmotes.listFiles((dir, name)->name.endsWith(".emote")))){
            EmoteInstance.instance.getLogger().log(Level.FINE, "[Quarktool]  Importing Quark emote: " + file.getName());
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                QuarkReader quarkReader = new QuarkReader();
                if(quarkReader.deserialize(reader, file.getName())){
                    EmoteHolder emote = quarkReader.getEmote();
                    EmoteHolder.addEmoteToList(emote);
                    File icon = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 6) + ".png").toFile();
                    if(icon.isFile()) emote.bindIcon(icon);
                }
            }catch(Throwable e){ //try to catch everything
                if(EmoteInstance.config.showDebug){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
