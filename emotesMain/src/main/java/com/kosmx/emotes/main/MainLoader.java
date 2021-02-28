package com.kosmx.emotes.main;

import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.main.config.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

public class MainLoader {
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
}
