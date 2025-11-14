package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.util.network.ProtocolUtils;

import java.util.List;

public class EmoteHeaderPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return PacketConfig.HEADER_PACKET;
    }

    @Override
    public byte getVer() {
        return 2;
    }

    @Override
    public void read(ByteBuf byteBuf, NetData config, byte version) {
        config.extraData.put("name", ProtocolUtils.readString(byteBuf));
        config.extraData.put("description", ProtocolUtils.readString(byteBuf));
        config.extraData.put("author", ProtocolUtils.readString(byteBuf));
        if (version >= 2) {
            config.extraData.put("folderpath", ProtocolUtils.readString(byteBuf));
            config.extraData.put("bages", ProtocolUtils.readList(byteBuf, ProtocolUtils::readString));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ByteBuf byteBuf, NetData config, byte version) {
        assert config.emoteData != null;
        ProtocolUtils.writeString(byteBuf, (String) config.emoteData.data().getRaw("name"));
        ProtocolUtils.writeString(byteBuf, (String) config.emoteData.data().getRaw("description"));
        ProtocolUtils.writeString(byteBuf, (String) config.emoteData.data().getRaw("author"));
        if (version >= 2) {
            ProtocolUtils.writeString(byteBuf, (String) config.emoteData.data().getRaw("folderpath"));
            ProtocolUtils.writeList(byteBuf, (List<String>) config.emoteData.data().getRaw("bages"), CommonNetwork::writeString);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.emoteData != null && config.purpose.exchangeHeader;
    }
}
