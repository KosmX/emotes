package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;

import java.io.OutputStream;

public interface ISerializer extends IReader {
    void write(Animation emote, OutputStream writer, String filename) throws EmoteSerializerException;
    boolean onlyEmoteFile();
}
