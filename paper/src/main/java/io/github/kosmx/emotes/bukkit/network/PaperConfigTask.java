package io.github.kosmx.emotes.bukkit.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.mc.network.ConfigTask;
import net.minecraft.network.protocol.Packet;

public class PaperConfigTask extends ConfigTask {
    @Override
    protected Packet<?> convert(EmotePacket packet) {
        return null;
    }
}
