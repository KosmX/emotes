package io.github.kosmx.emotes.common.network;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * I can't use Minecraft's string and uuid byte reader in a bukkit plugin, I need to implement these.
 * This can still here but it can be removed if unused
 */
public class CommonNetwork {
    public static String readString(ByteBuffer buf) throws IOException {
        int len = buf.getInt();
        if(len < 0){
            throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
        }
        byte[] b = new byte[len];
        buf.get(b); //that is safe to use.

        return new String(b, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuffer buf, String str){
        byte[] b = str.getBytes(StandardCharsets.UTF_8);
        buf.putInt(b.length);
        buf.put(b);
    }

    //copied from MC
    public static String readVarString(ByteBuffer buf) throws IOException {
        int j = readVarInt(buf);
        if (j < 0) {
            throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            byte[] bytes = new byte[j];
            buf.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
    public static void writeVarString(ByteBuffer buf, String str){
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.put(bytes);
    }

    public static UUID readUUID(ByteBuffer buf){
        long a = buf.getLong();
        long b = buf.getLong();
        return new UUID(a, b); //The order is important
    }
    public static void writeUUID(ByteBuffer buf, UUID uuid){
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
    }

    //copied from minecraft
    public static int readVarInt(ByteBuffer buf) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buf.get();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }

    //copied from minecraft
    public static void writeVarInt(ByteBuffer buf, int i){
        while((i & - 128) != 0){
            buf.put((byte) (i & 127 | 128));
            i >>>= 7;
        }

        buf.put((byte) i);
    }
}
