package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.util.logging.Level;

/**
 * Emotecraft's loader
 */
public class MainLoader {
    static int tick = 0;
    //The main mod-loader class
    public static void main(boolean isClient){

        if(CommonData.isLoaded){
            LoggerService.INSTANCE.log(Level.SEVERE, "Emotecraft is loaded multiple times, please load it only once!");
        }
        CommonData.isLoaded = true;

        //This data is available at server-side.
        Serializer.INSTANCE = isClient ? new ClientSerializer() : new Serializer();

        //TODO init server networking on actual implementation

        if(isClient) {
            MainClientInit.init();
        }else UniversalEmoteSerializer.loadEmotes();

    }

    public static void tick(){
        if(tick++ >= 20){
            tick=0;

            ClientEmotePlay.checkQueue();
        }
    }
}
