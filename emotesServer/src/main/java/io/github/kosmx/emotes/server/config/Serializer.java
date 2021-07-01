package io.github.kosmx.emotes.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Serializer {
    public static Gson serializer;

    public static Serializer INSTANCE;

    public Serializer(){
        initializeSerializer();
    }

    public void initializeSerializer(){
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        this.registerTypeAdapters(builder);
        serializer = builder.create();
    }

    protected void registerTypeAdapters(GsonBuilder builder){
        JsonDeserializer<SerializableConfig> configSerializer = new ConfigSerializer();
        JsonSerializer<SerializableConfig> configDeserializer = new ConfigSerializer();
        builder.registerTypeAdapter(SerializableConfig.class, configDeserializer);
        builder.registerTypeAdapter(SerializableConfig.class, configSerializer);
    }

    public static void saveConfig(){
        try{
            BufferedWriter writer = Files.newBufferedWriter(EmoteInstance.instance.getConfigPath());
            serializer.toJson(EmoteInstance.config, writer);
            writer.close();
            //FileUtils.write(Main.CONFIGPATH, Serializer.serializer.toJson(Main.config), "UTF-8", false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
