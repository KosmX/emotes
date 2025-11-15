package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;

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
        config.extraData.put("name", CommonNetwork.readString(byteBuf));
        config.extraData.put("description", CommonNetwork.readString(byteBuf));
        config.extraData.put("author", CommonNetwork.readString(byteBuf));
        if (version >= 2) {
            config.extraData.put("folderpath", CommonNetwork.readString(byteBuf));
            config.extraData.put("bages", CommonNetwork.readList(byteBuf, CommonNetwork::readString));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ByteBuf byteBuf, NetData config, byte version) {
        assert config.emoteData != null;
        CommonNetwork.writeString(byteBuf, (String) config.emoteData.data().getRaw("name"));
        CommonNetwork.writeString(byteBuf, (String) config.emoteData.data().getRaw("description"));
        CommonNetwork.writeString(byteBuf, (String) config.emoteData.data().getRaw("author"));
        if (version >= 2) {
            CommonNetwork.writeString(byteBuf, (String) config.emoteData.data().getRaw("folderpath"));
            CommonNetwork.writeList(byteBuf, (List<String>) config.emoteData.data().getRaw("bages"), CommonNetwork::writeString);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.emoteData != null && config.purpose.exchangeHeader;
    }
}
