package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientConfigSerializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
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

        if (isClient) {
            Serializer.INSTANCE = new Serializer<>(new ClientConfigSerializer(), ClientConfig.class);
            MainClientInit.init();

        } else {
            Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
            UniversalEmoteSerializer.loadEmotes();
        }
    }

    public static void tick(){
        if(tick++ >= 20){
            tick=0;

            ClientEmotePlay.checkQueue();
        }
    }
}
