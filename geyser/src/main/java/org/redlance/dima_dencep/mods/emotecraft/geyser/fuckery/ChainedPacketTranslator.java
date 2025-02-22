package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import org.geysermc.geyser.session.GeyserSession;

import java.io.IOException;

@FunctionalInterface
public interface ChainedPacketTranslator<T> {
    boolean translate(GeyserSession session, T packet) throws IOException;
}
