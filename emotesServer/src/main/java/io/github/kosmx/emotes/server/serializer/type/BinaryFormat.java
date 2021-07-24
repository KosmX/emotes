package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BinaryFormat implements ISerializer{
    @Override
    public List<EmoteData> read(InputStream stream, String filename) throws EmoteSerializerException {
        try {
            NetData data = new EmotePacket.Builder().build().read(readFromIStream(stream));
            if(data.purpose != PacketTask.FILE) throw new EmoteSerializerException("Binary emote is invalid", getFormatExtension());
            List<EmoteData> list = new ArrayList<>(1);
            list.add(data.emoteData);
            return list;
        }catch (IOException | NullPointerException exception){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), exception);
        }
    }

    @Override
    public void write(EmoteData emote, OutputStream stream) throws IOException {
        try{
            ByteBuffer byteBuffer = new EmotePacket.Builder().configureToSaveEmote(emote).build().write();
            stream.write(AbstractNetworkInstance.safeGetBytesFromBuffer(byteBuffer));
        }catch (Exception e){
            throw new EmoteSerializerException("Something went wrong", getFormatExtension(), e);
        }
    }

    @Override
    public EmoteFormat getFormatType() {
        return EmoteFormat.BINARY;
    }

    private ByteBuffer readFromIStream(InputStream stream) throws IOException {
        List<Pair<Integer, byte[]>> listOfBites = new LinkedList<>();
        int totalSize = 0;
        while (true){
            int estimatedSize = stream.available();
            byte[] bytes = new byte[Math.max(1, estimatedSize)];
            int i = stream.read(bytes);
            if(i < 1) break;
            totalSize += i;
            listOfBites.add(new Pair<>(i, bytes));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
        for(Pair<Integer, byte[]> i:listOfBites){
            byteBuffer.put(i.getRight(), 0, i.getLeft());
        }
        return byteBuffer;
    }
}
