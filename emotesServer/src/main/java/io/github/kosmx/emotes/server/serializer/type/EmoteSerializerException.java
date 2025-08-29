package io.github.kosmx.emotes.server.serializer.type;

import org.jetbrains.annotations.Nullable;

public class EmoteSerializerException extends RuntimeException {
    @Nullable
    private final String type;

    public EmoteSerializerException(String msg, @Nullable String type) {
        super(msg);
        this.type = type;
    }

    public EmoteSerializerException(String msg, @Nullable String type, Throwable cause) {
        super(msg, cause);
        this.type = type;
    }

    @Nullable
    public String getType() {
        return this.type;
    }
}
