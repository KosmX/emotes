package io.github.kosmx.emotes.server.serializer.type;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.server.config.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class JsonEmoteWrapper implements ISerializer {

    @Override
    public List<EmoteData> read(BufferedReader reader, String filename) throws EmoteSerializerException {
        try{
            List<EmoteData> emotes = Serializer.serializer.fromJson(reader, new TypeToken<List<EmoteData>>(){}.getType());
            //TODO bind icons
            return emotes;
        }catch (JsonParseException e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }

    @Override
    public void write(EmoteData data) throws IOException {

    }

    @Override
    public String getFormatExtension() {
        return "json";
    }
}
