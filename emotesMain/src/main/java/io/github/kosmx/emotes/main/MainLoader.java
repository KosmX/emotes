package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.Serializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
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
        Serializer.initializeSerializer();

        loadConfig();

        //TODO init server networking
        if(EmoteInstance.instance.isClient()) {
            ClientInit.init();
        }
    }

    public static void loadConfig(){
        if(EmoteInstance.instance.getConfigPath().toFile().isFile()) {
            try {
                BufferedReader reader = Files.newBufferedReader(EmoteInstance.instance.getConfigPath());
                EmoteInstance.config = Serializer.serializer.fromJson(reader, SerializableConfig.class);
                reader.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        else {
            EmoteInstance.config = EmoteInstance.instance.isClient() ? new ClientConfig() : new SerializableConfig();
            EmoteInstance.instance.getLogger().log(Level.FINE, "Creating new config file for Emotecraft");
            Serializer.saveConfig();
        }
        if(EmoteInstance.config == null) {
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
