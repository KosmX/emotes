package io.github.kosmx.emotes.server.serializer.type;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BinaryFormat implements ISerializer{
    @Override
    public List<KeyframeAnimation> read(InputStream stream, String filename) throws EmoteSerializerException {
        try {
            NetData data = new EmotePacket.Builder().build().read(MathHelper.readFromIStream(stream));
            if(data.purpose != PacketTask.FILE) throw new EmoteSerializerException("Binary emote is invalid", getFormatExtension());
            List<KeyframeAnimation> list = new ArrayList<>(1);
            assert data.emoteData != null;
            list.add(data.emoteData);
            return list;
        }catch (IOException | NullPointerException exception){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), exception);
        }
    }

    @Override
    public void write(KeyframeAnimation emote, OutputStream stream) throws EmoteSerializerException {
        try{
            ByteBuffer byteBuffer = new EmotePacket.Builder().configureToSaveEmote(emote).build().write();
            stream.write(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer(byteBuffer)));
        }catch (Exception e){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), e);
        }
    }

    @Override
    public AnimationFormat getFormatType() {
        return AnimationFormat.BINARY;
    }
}
