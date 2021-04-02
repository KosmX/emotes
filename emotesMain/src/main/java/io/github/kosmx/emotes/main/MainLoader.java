package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.Serializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

public class MainLoader {
    static int tick = 0;
    //The main mod-loader class
    public static void main(String[] args){

        Serializer.initializeSerializer();

        loadConfig();

        //TODO init server networking
        if(EmoteInstance.instance.isClient()) {
            ClientInit.init();
        }
    }

    public static void loadConfig(){
        try {
            BufferedReader reader = Files.newBufferedReader(EmoteInstance.instance.getConfigPath());
            EmoteInstance.config = Serializer.serializer.fromJson(reader, SerializableConfig.class);
            reader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if(EmoteInstance.config == null){
            EmoteInstance.config = EmoteInstance.instance.isClient() ? new ClientConfig() : new SerializableConfig();
        }
    }

    public static void tick(){
        if(tick++ >= 20){
            tick=0;

            ClientEmotePlay.checkQueue();
        }
    }
}
