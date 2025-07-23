package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    public void read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.extraData.put("name", CommonNetwork.readString(byteBuffer));
        config.extraData.put("description", CommonNetwork.readString(byteBuffer));
        config.extraData.put("author", CommonNetwork.readString(byteBuffer));
        if (version >= 2) {
            config.extraData.put("folderpath", CommonNetwork.readString(byteBuffer));
            config.extraData.put("bages", CommonNetwork.readList(byteBuffer, CommonNetwork::readString));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        assert config.emoteData != null;
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.data().getRaw("name"));
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.data().getRaw("description"));
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.data().getRaw("author"));
        if (getVer(config.versions) >= 2) {
            CommonNetwork.writeString(byteBuffer, (String) config.emoteData.data().getRaw("folderpath"));
            CommonNetwork.writeList(byteBuffer, (List<String>) config.emoteData.data().getRaw("bages"), CommonNetwork::writeString);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.emoteData != null && config.purpose.exchangeHeader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int calculateSize(NetData config) {
        Animation emote = config.emoteData;
        if (emote == null) return 0;

        int baseSize = sumStrings(
                (String) emote.data().getRaw("name"),
                (String) emote.data().getRaw("description"),
                (String) emote.data().getRaw("author")
        );

        if (getVer(config.versions) >= 2) {
            baseSize += CommonNetwork.stringSize((String) emote.data().getRaw("folderpath"));
            baseSize += CommonNetwork.listSize((List<String>) config.emoteData.data().getRaw("bages"), CommonNetwork::stringSize);
        }
        return baseSize;
    }

    public static int sumStrings(String... strings) {
        int size = 0;
        for (String s : strings) {
            size += CommonNetwork.stringSize(s);
        }
        return size;
    }
}
