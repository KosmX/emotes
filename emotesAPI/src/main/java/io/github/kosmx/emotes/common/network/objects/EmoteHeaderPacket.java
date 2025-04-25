package io.github.kosmx.emotes.common.network.objects;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.network.CommonNetwork;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class EmoteHeaderPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return 0x11;
    }

    @Override
    public byte getVer() {
        return 2;
    }

    @Override
    public boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        config.extraData.put("name", CommonNetwork.readString(byteBuffer));
        config.extraData.put("description", CommonNetwork.readString(byteBuffer));
        config.extraData.put("author", CommonNetwork.readString(byteBuffer));
        if (version >= 2) {
            config.extraData.put("folderpath", CommonNetwork.readString(byteBuffer));
            config.extraData.put("bages", CommonNetwork.readList(byteBuffer, CommonNetwork::readString));
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        assert config.emoteData != null;
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.extraData.get("name"));
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.extraData.get("description"));
        CommonNetwork.writeString(byteBuffer, (String) config.emoteData.extraData.get("author"));
        if (getVer(config.versions) >= 2) {
            CommonNetwork.writeString(byteBuffer, (String) config.emoteData.extraData.get("folderpath"));
            CommonNetwork.writeList(byteBuffer, (List<String>) config.emoteData.extraData.get("bages"), CommonNetwork::writeString);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.emoteData != null && config.purpose.exchangeHeader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int calculateSize(NetData config) {
        KeyframeAnimation emote = config.emoteData;
        if (emote == null) return 0;

        int baseSize = sumStrings(
                (String) emote.extraData.get("name"),
                (String) emote.extraData.get("description"),
                (String) emote.extraData.get("author")
        );

        if (getVer(config.versions) >= 2) {
            baseSize += CommonNetwork.stringSize((String) emote.extraData.get("folderpath"));
            baseSize += CommonNetwork.listSize((List<String>) config.emoteData.extraData.get("bages"), CommonNetwork::stringSize);
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
