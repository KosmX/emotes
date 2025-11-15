package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class BinaryFormat implements ISerializer {
    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Override
    public List<Animation> read(InputStream stream, String filename) throws EmoteSerializerException {
        ByteBuf buf = ALLOC.buffer();
        try {
            buf.writeBytes(stream.readAllBytes());

            NetData data = new EmotePacket(buf).data;
            if (data.purpose != PacketTask.FILE || data.emoteData == null) {
                throw new EmoteSerializerException("Binary emote is invalid", getExtension());
            }
            return Collections.singletonList(data.emoteData);
        } catch (Throwable exception) {
            throw new EmoteSerializerException("Something went wrong", getExtension(), exception);
        } finally {
            buf.release();
        }
    }

    @Override
    public void write(Animation emote, OutputStream stream, String filename) throws EmoteSerializerException {
        ByteBuf buf = ALLOC.buffer();
        try {
            new EmotePacket.Builder().strictSizeLimit(false).configureToSaveEmote(emote).build().write(buf, ALLOC);
            buf.readBytes(stream, buf.readableBytes());
        } catch (Throwable e){
            throw new EmoteSerializerException("Something went wrong", getExtension(), e);
        } finally {
            buf.release();
        }
    }

    @Override
    public boolean onlyEmoteFile() {
        return false;
    }

    @Override
    public String getExtension() {
        return "emotecraft";
    }
}
