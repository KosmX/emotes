package io.github.kosmx.emotes.arch.network.server.instance;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.instance.ServerNetworkInstance;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class McServerNetworkInstance extends ServerNetworkInstance {
    @NotNull
    public abstract Avatar getAvatar();

    @Override
    public UUID getUUID() {
        return getAvatar().getUUID();
    }

    @Override
    public boolean isTrackingPlayState() {
        return true; // MC server does track this
    }

    @Override
    public int maxDataSize() {
        return CommonData.MAX_PACKET_SIZE - 16; // channel ID is 12, one extra int makes it 16 (string)
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, boolean updateVersions) {
        if (updateVersions) builder.setVersion(getVersions());
        sendPlayMessage(builder.build());
    }

    public abstract void sendPlayMessage(EmotePacket bytes);
}
