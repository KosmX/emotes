package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;

import java.io.OutputStream;

public interface IWriter extends ISerializer {
    void write(Animation emote, OutputStream writer, String filename) throws EmoteSerializerException;

    boolean onlyEmoteFile();
    boolean possibleDataLoss();

    default boolean canWrite(String fileName) {
        return fileName != null && fileName.endsWith("." + getExtension());
    }
}
