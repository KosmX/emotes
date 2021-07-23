package io.github.kosmx.emotes.server.serializer.type;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.server.config.Serializer;

import java.io.*;
import java.util.List;

public class JsonEmoteWrapper implements ISerializer {

    @Override
    public List<EmoteData> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        BufferedReader reader = streamReader(inputStream);
        try{
            return Serializer.serializer.fromJson(reader, new TypeToken<List<EmoteData>>(){}.getType());
        }catch (JsonParseException e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }

    @Override
    public void write(EmoteData emote, OutputStream outputStream) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        Serializer.serializer.toJson(emote, bufferedWriter);
    }

    @Override
    public String getFormatExtension() {
        return "json";
    }
}
