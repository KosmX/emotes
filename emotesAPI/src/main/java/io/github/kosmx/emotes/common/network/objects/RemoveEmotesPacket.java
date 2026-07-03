package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.network.NetworkUtils;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.util.network.ProtocolUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RemoveEmotesPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.REMOVE_EMOTE_PACKET;
    }

    @Override
    public byte getVer() {
        return 0;
    }

    @Override
    public void read(ByteBuf byteBuf, NetData config, byte version) throws IOException {
        List<UUID> removeEmoteIds = ProtocolUtils.readList(byteBuf, NetworkUtils::readUuid);
        config.removeEmoteIds.clear();
        config.removeEmoteIds.addAll(removeEmoteIds);
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) throws IOException {
        ProtocolUtils.writeList(buf, config.removeEmoteIds,
                (id, buf2) -> NetworkUtils.writeUuid(buf2, id)
        );
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.REMOVE && !config.removeEmoteIds.isEmpty();
    }

    @Override
    public Set<PacketBound> boundsTo() {
        return PacketBound.TO_CLIENT;
    }
}
