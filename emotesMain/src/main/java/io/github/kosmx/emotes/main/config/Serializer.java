package io.github.kosmx.emotes.main.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.common.SerializableConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Serializer {

    public static Gson serializer;

    public static void initializeSerializer(){
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();

        JsonDeserializer<SerializableConfig> configSerializer = new ConfigSerializer();
        JsonSerializer<SerializableConfig> configDeserializer = new ConfigSerializer();
        JsonDeserializer<List<EmoteHolder>> emoteDeserializer = new EmoteSerializer();
        JsonSerializer<EmoteHolder> emoteSerializer = new EmoteSerializer();
        builder.registerTypeAdapter(SerializableConfig.class, configDeserializer);
        builder.registerTypeAdapter(SerializableConfig.class, configSerializer);
        builder.registerTypeAdapter(ClientConfig.class, configSerializer);
        builder.registerTypeAdapter(new TypeToken<List<EmoteHolder>>(){}.getType(), emoteDeserializer);
        builder.registerTypeAdapter(EmoteHolder.class, emoteSerializer);

        serializer = builder.create();
    }

    public static void saveConfig(){
        try{
            BufferedWriter writer = Files.newBufferedWriter(EmoteInstance.instance.getConfigPath());
            Serializer.serializer.toJson(EmoteInstance.config, writer);
            writer.close();
            //FileUtils.write(Main.CONFIGPATH, Serializer.serializer.toJson(Main.config), "UTF-8", false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
