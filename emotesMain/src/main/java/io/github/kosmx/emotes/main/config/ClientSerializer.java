package io.github.kosmx.emotes.main.config;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;

import java.io.BufferedReader;
import java.util.List;

public class ClientSerializer extends Serializer {


    @Override
    public void registerTypeAdapters(GsonBuilder builder){
        super.registerTypeAdapters(builder);
        builder.registerTypeAdapter(ClientConfig.class, new ClientConfigSerializer());
    }

    @Override
    protected SerializableConfig readConfig(BufferedReader reader) throws JsonSyntaxException, JsonIOException {
        if(reader != null) return serializer.fromJson(reader, ClientConfig.class);
        return new ClientConfig();
    }


}
