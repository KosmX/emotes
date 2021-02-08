package com.kosmx.emotecraftCommon.network;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * I can't use Minecraft's string and uuid byte reader in a bukkit plugin, I need to implement these.
 */
public class CommonNetwork {
    public static String readString(ByteBuf buf){
        int len = buf.readInt();
        if(len < 0){
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        String str = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + len);
        return str;
    }
    public static void writeString(ByteBuf buf, String str){
    }

    //copied from MC
    public static String readVarString(ByteBuf buf){
        int j = readVarInt(buf);
        if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String string = buf.toString(buf.readerIndex(), j, StandardCharsets.UTF_8);
            buf.readerIndex(buf.readerIndex() + j);
            return string;
        }
    }
    public static void writeVarString(ByteBuf buf, String str){
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static UUID readUUID(ByteBuf buf){
        return new UUID(buf.readLong(), buf.readLong());
    }
    public static void writeUUID(ByteBuf buf, UUID uuid){
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    //copied from minecraft
    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buf.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }

    //copied from minecraft
    public static void writeVarInt(ByteBuf buf, int i){
        while((i & - 128) != 0){
            buf.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        buf.writeByte(i);
    }
}
