package io.github.kosmx.emotes.common.network;

import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.util.network.ProtocolUtils;

/**
 * I can't use Minecraft's string and uuid byte reader in a bukkit plugin, I need to implement these.
 * This can still here but it can be removed if unused
 */
public class CommonNetwork {
    public static boolean readBoolean(ByteBuf buf) {
        return buf.readByte() != 0;
    }

    public static void writeBoolean(ByteBuf buf, boolean bool) {
        buf.writeByte((byte) (bool ? 1 : 0));
    }

    public static void writeString(String str, ByteBuf buf) {
        ProtocolUtils.writeString(buf, str);
    }
}
