package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;

import java.io.InputStream;
import java.util.Map;

public interface IReader extends ISerializer {
    Map<String, Animation> read(InputStream reader, String filename) throws EmoteSerializerException;

    default boolean canRead(String fileName) {
        return fileName != null && fileName.endsWith("." + getExtension());
    }
}
