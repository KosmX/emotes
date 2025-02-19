package io.github.kosmx.emotes.main;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Initializing client and other load stuff...
 *
 */
public class MainClientInit {

    public static void init(){
        loadEmotes();//:D

        ClientPacketManager.init(); //initialize proxy service
        ClientEmotePlay.init();
    }


    public static void loadEmotes() {
        UniversalEmoteSerializer.loadEmotes();

        EmoteHolder.clearEmotes();

        EmoteHolder.addEmoteToList(UniversalEmoteSerializer.hiddenServerEmotes);

    }

    /**
     * play the test emote
     */
    @SuppressWarnings({"deprecation","removal"})
    public static void playDebugEmote(){
        LoggerService.LOADED_SERVICE.log(Level.INFO, "Playing debug emote");
        Path location = null;
        for(AnimationFormat source:AnimationFormat.values()){
            location = InstanceService.LOADED_SERVICE.getGameDirectory().resolve("emote." + source.getExtension());
            if(location.toFile().isFile()){
                break;
            }
        }
        if(location == null)return;
        try{
            InputStream reader = Files.newInputStream(location);
            EmoteHolder emoteHolder = new EmoteHolder(UniversalEmoteSerializer.readData(reader, location.getFileName().toString()).getFirst());
            reader.close();
            if(TmpGetters.getClientMethods().getMainPlayer() != null){
                emoteHolder.playEmote(TmpGetters.getClientMethods().getMainPlayer());
            }
        }catch(Exception e){
            LoggerService.LOADED_SERVICE.log(Level.WARNING, "Error while importing debug emote.", e);
        }
    }
}
