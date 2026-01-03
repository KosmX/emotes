package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.redlance.common.utils.ReflectUtils;

import java.lang.invoke.VarHandle;

public class ProtocolUtils {
    private static final VarHandle GEYSER_SESSION_PROTOCOL = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findVarHandle(
            GeyserSession.class, "protocol", MinecraftProtocol.class
    ));

    public static MinecraftProtocol getProtocol(GeyserSession session) {
        return (MinecraftProtocol) GEYSER_SESSION_PROTOCOL.get(session);
    }
}
