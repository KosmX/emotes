package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;

public interface ProtocolProvider {
    MinecraftProtocol ec$protocol();

    default ProtocolState ec$state() {
        return ec$protocol().getOutboundState();
    }
}
