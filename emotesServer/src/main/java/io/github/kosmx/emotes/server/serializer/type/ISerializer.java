package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.common.emote.EmoteData;

import java.io.BufferedWriter;
import java.io.IOException;

public interface ISerializer extends IReader {
    void write(EmoteData emote, BufferedWriter writer) throws IOException;
}
