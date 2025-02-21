package io.github.kosmx.emotes.server.serializer.type;

public class EmoteSerializerException extends RuntimeException {
    private final String type;

    public EmoteSerializerException(String msg, String type) {
        super(msg);
        this.type = type;
    }

    public EmoteSerializerException(String msg, String type, Throwable cause) {
        super(msg, cause);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
