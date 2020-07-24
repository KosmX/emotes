package com.kosmx.emotecraft.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class Serializer {

    public static Gson serializer;

    public static void initializeSerializer() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();

        JsonDeserializer<SerializableConfig> configSerializer = new ConfigSerializer();
        JsonSerializer<SerializableConfig> configDeserializer = new ConfigSerializer();
        JsonDeserializer<EmoteHolder> emoteDeserializer = new EmoteSerializer();
        builder.registerTypeAdapter(SerializableConfig.class, configDeserializer);
        builder.registerTypeAdapter(SerializableConfig.class, configSerializer);
        builder.registerTypeAdapter(EmoteHolder.class, emoteDeserializer);

        serializer = builder.create();
    }
}
