package io.github.kosmx.emotes.common.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GeyserEmotePacket {
    private long runtimeEntityID;
    private UUID emoteID;

    public void read(byte[] bytes) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        runtimeEntityID = 0;
        byte[] str = new byte[byteBuffer.get()];
        byteBuffer.get(str);
        emoteID = UUID.fromString(new String(str, StandardCharsets.UTF_8));
    }

    public byte[] write() throws IOException {
        byte[] bytes = emoteID.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length + 1 + 8);
        byteBuffer.put((byte) bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.putLong(runtimeEntityID);
        return byteBuffer.array();
    }

    public long getRuntimeEntityID() {
        return runtimeEntityID;
    }

    public void setEmoteID(UUID emoteID) {
        this.emoteID = emoteID;
    }

    public UUID getEmoteID() {
        return emoteID;
    }

    public void setRuntimeEntityID(long runtimeEntityID) {
        this.runtimeEntityID = runtimeEntityID;
    }
}
