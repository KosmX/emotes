package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.netty.buffer.ByteBuf;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.io.IOException;

public class EmoteIconPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return PacketConfig.ICON_PACKET;
    }

    @Override
    public byte getVer() {
        return 0x13;
    }

    @Override
    public void read(ByteBuf byteBuf, NetData config, byte version) throws IOException {
        int size = byteBuf.readInt();
        if (size == 0 || (size < 0 && version < 0x13)) return;

        byte[] iconData = MathHelper.readBytes(byteBuf, Math.abs(size));

        if (size < 0) { // Negative size = WebP, positive size = PNG
            config.extraData.put("iconData", PlatformWebPDecoder.INSTANCE.decode(iconData));
        } else {
            config.extraData.put("iconData", DecodedImage.fromPng(iconData));
        }
    }

    @Override
    public void write(ByteBuf byteBuf, NetData config, byte version) throws IOException {
        assert config.emoteData != null;

        DecodedImage iconData = config.emoteData.data().getImage("iconData");
        if (iconData == null) {
            byteBuf.writeInt(0);
            return;
        }

        if (version >= 0x13 && PlatformWebPEncoder.INSTANCE.isAvailable()) {
            byte[] webpData = PlatformWebPEncoder.INSTANCE.encodeLossless(iconData);
            byteBuf.writeInt(-webpData.length);
            byteBuf.writeBytes(webpData);
        } else {
            byte[] pngData = iconData.toPng();
            byteBuf.writeInt(pngData.length);
            byteBuf.writeBytes(pngData);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.purpose == PacketTask.FILE && config.emoteData != null && config.emoteData.data().has("iconData");
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
