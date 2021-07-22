package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.common.emote.EmoteData;

import java.io.BufferedReader;
import java.util.List;

public interface IReader {
    List<EmoteData> read(BufferedReader reader, String filename) throws EmoteSerializerException;
    String getFormatExtension();
}
