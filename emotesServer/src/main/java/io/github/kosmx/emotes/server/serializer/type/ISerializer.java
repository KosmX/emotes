package io.github.kosmx.emotes.server.serializer.type;

import org.redlance.common.services.AdvancedService;

public interface ISerializer extends AdvancedService {
    String getExtension();

    @Override
    default boolean isServiceActive() {
        return getExtension() != null;
    }
}
