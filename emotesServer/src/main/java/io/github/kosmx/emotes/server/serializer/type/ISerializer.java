package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.api.services.IEmotecraftService;

public interface ISerializer extends IEmotecraftService {
    String getExtension();

    @Override
    default boolean isActive() {
        return getExtension() != null;
    }
}
