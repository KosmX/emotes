package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.services.IEmotecraftService;

import java.io.InputStream;
import java.util.List;

public interface IReader extends IEmotecraftService {
    List<Animation> read(InputStream reader, String filename) throws EmoteSerializerException;

    default boolean canRead(String fileName) {
        return fileName != null && fileName.endsWith("." + getExtension());
    }

    String getExtension();

    @Override
    default boolean isActive() {
        return getExtension() != null;
    }
}
