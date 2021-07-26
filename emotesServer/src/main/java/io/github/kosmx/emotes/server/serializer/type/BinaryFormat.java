package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.MathHelper;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BinaryFormat implements ISerializer{
    @Override
    public List<EmoteData> read(InputStream stream, String filename) throws EmoteSerializerException {
        try {
            NetData data = new EmotePacket.Builder().build().read(MathHelper.readFromIStream(stream));
            if(data.purpose != PacketTask.FILE) throw new EmoteSerializerException("Binary emote is invalid", getFormatExtension());
            List<EmoteData> list = new ArrayList<>(1);
            assert data.emoteData != null;
            list.add(data.emoteData);
            return list;
        }catch (IOException | NullPointerException exception){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), exception);
        }
    }

    @Override
    public void write(EmoteData emote, OutputStream stream) throws EmoteSerializerException {
        try{
            ByteBuffer byteBuffer = new EmotePacket.Builder().configureToSaveEmote(emote).build().write();
            stream.write(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer(byteBuffer)));
        }catch (Exception e){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), e);
        }
    }

    @Override
    public EmoteFormat getFormatType() {
        return EmoteFormat.BINARY;
    }
}
