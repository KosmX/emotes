package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.common.emote.EmoteData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public interface IReader {
    List<EmoteData> read(InputStream reader, String filename) throws EmoteSerializerException;
    String getFormatExtension();

    default BufferedReader streamReader(InputStream stream){
        return new BufferedReader(new InputStreamReader(stream));
    }
}
