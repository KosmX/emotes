package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.opennbs.NBSFileUtils;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
        serializeExternalEmotes();

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

    private static void serializeExternalEmotes(){
        File externalEmotes = EmoteInstance.instance.getExternalEmoteDir();
        for(File file : Objects.requireNonNull(EmoteInstance.instance.getExternalEmoteDir().listFiles((dir, name)->name.endsWith(".json")))){
            serializeExternalEmote(file);
        }
        for(File file : Objects.requireNonNull(EmoteInstance.instance.getExternalEmoteDir().listFiles((dir, name)->name.endsWith("." + EmoteFormat.BINARY.getExtension())))){
            serializeExternalEmote(file);
        }

        if(((ClientConfig)EmoteInstance.config).enableQuark.get()){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Quark importer is active", true);
            for(File file : Objects.requireNonNull(EmoteInstance.instance.getExternalEmoteDir().listFiles((dir, name)->name.endsWith(".emote")))){
                serializeExternalEmote(file);
            }
        }
    }

    private static void serializeExternalEmote(File file){
        File externalEmotes = EmoteInstance.instance.getExternalEmoteDir();
        try{
            InputStream reader = Files.newInputStream(file.toPath());
            List<EmoteData> emotes = UniversalEmoteSerializer.readData(reader, file.getName());
            EmoteHolder.addEmoteToList(emotes);
            reader.close();
            Path icon = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".png");

            if(icon.toFile().isFile()){
                InputStream iconStream = Files.newInputStream(icon);
                emotes.forEach(emote -> {
                    try {
                        emote.iconData = MathHelper.readFromIStream(iconStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            File song = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".nbs").toFile();
            if(song.isFile() && emotes.size() == 1){
                DataInputStream bis = new DataInputStream(new FileInputStream(song));
                try {
                    emotes.get(0).song = NBSFileUtils.read(bis);
                }
                catch (IOException exception){
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while reading song: " + exception.getMessage(), true);
                    if(EmoteInstance.config.showDebug.get()) exception.printStackTrace();
                }
                bis.close(); //I almost forgot this
            }
        }catch(Exception e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while importing external emote: " + file.getName() + ".", true);
            EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage());
            if(EmoteInstance.config.showDebug.get())e.printStackTrace();
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
