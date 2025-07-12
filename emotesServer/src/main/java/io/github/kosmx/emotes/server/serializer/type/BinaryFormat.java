package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.MathHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BinaryFormat implements ISerializer {
    @Override
    public List<Animation> read(InputStream stream, String filename) throws EmoteSerializerException {
        try {
            NetData data = new EmotePacket.Builder().strictSizeLimit(false).build().read(MathHelper.readFromIStream(stream));
            if (data.purpose != PacketTask.FILE || data.emoteData == null) {
                throw new EmoteSerializerException("Binary emote is invalid", getExtension());
            }
            return Collections.singletonList(data.emoteData);
        } catch (Throwable exception) {
            throw new EmoteSerializerException("Something went wrong", getExtension(), exception);
        }
    }

    @Override
    public void write(Animation emote, OutputStream stream, String filename) throws EmoteSerializerException {
        try {
            ByteBuffer byteBuffer = new EmotePacket.Builder().strictSizeLimit(false).configureToSaveEmote(emote).build().write();
            stream.write(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer(byteBuffer)));
        } catch (Throwable e){
            throw new EmoteSerializerException("Something went wrong", getExtension(), e);
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
