package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public interface IReader {
    List<EmoteData> read(InputStream reader, String filename) throws EmoteSerializerException;

    default String getFormatExtension(){
        return getFormatType().getExtension();
    }

    EmoteFormat getFormatType();

    default BufferedReader streamReader(InputStream stream){
        return new BufferedReader(new InputStreamReader(stream));
    }
}
