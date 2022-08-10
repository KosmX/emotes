package io.github.kosmx.emotes.server.serializer.type;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.io.OutputStream;

public interface ISerializer extends IReader {
    void write(KeyframeAnimation emote, OutputStream writer) throws EmoteSerializerException;
}
