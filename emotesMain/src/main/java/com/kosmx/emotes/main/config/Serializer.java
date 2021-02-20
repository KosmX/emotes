package com.kosmx.emotes.main.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.common.SerializableConfig;

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
        builder.registerTypeAdapter(new TypeToken<List<EmoteHolder>>(){}.getType(), emoteDeserializer);
        builder.registerTypeAdapter(EmoteHolder.class, emoteSerializer);

        serializer = builder.create();
    }
}
