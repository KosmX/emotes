package io.github.kosmx.emotes.main.config;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.config.Serializer;

import java.io.BufferedReader;

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
