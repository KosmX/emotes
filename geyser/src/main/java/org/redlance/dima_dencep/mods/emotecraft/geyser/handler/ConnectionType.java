package org.redlance.dima_dencep.mods.emotecraft.geyser.handler;

import org.jetbrains.annotations.Nullable;

public enum ConnectionType {
    NONE("emotecraft.no_server"),
    BACKEND(null)/*,
    PROXY("emotecraft.only_proxy")*/;

    @Nullable
    public final String translation;

    ConnectionType(@Nullable String translation) {
        this.translation = translation;
    }
}
