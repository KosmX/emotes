package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
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
    public static void main(String[] args){

        if(CommonData.isLoaded){
            EmoteInstance.instance.getLogger().log(Level.SEVERE, "Emotecraft is loaded multiple times, please load it only once!", true);
        }
        CommonData.isLoaded = true;

        //This data is available at server-side.
        Serializer.INSTANCE = EmoteInstance.instance.isClient() ? new ClientSerializer() : new Serializer();

        EmoteInstance.config = Serializer.getConfig();

        if(! EmoteInstance.instance.getExternalEmoteDir().isDirectory()) EmoteInstance.instance.getExternalEmoteDir().mkdirs();

        //TODO init server networking on actual implementation

        if(EmoteInstance.instance.isClient()) {
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
