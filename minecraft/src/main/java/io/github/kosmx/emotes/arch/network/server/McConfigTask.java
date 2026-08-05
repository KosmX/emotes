package io.github.kosmx.emotes.arch.network.server;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.mc.network.ConfigTask;
import net.minecraft.network.protocol.Packet;

public class McConfigTask extends ConfigTask {
    @Override
    protected Packet<?> convert(EmotePacket packet) {
        return NetworkPlatformTools.playPacket(packet);
    }
}
