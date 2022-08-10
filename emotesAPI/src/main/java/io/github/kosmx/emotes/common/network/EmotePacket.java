package io.github.kosmx.emotes.common.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Send everything emotes mod data...
 */
public class EmotePacket {
    public static final HashMap<Byte, Byte> defaultVersions = new HashMap<>();

    static {
        AbstractNetworkPacket tmp = new EmoteDataPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new PlayerDataPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new DiscoveryPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new StopPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new SongPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new EmoteHeaderPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new EmoteIconPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
    }

    public final NetHashMap subPackets = new NetHashMap();

    public final NetData data;

    int version;

    protected EmotePacket(@Nonnull NetData data) {
        //Make sure every packet has a version...
        if(data.versions == null)data.versions = new HashMap<>();
        defaultVersions.forEach((aByte, bByte) -> {
            if(!data.versions.containsKey(aByte)){
                data.versions.put(aByte, bByte);
            }
        });

        this.data = data;
        subPackets.put(new EmoteDataPacket());
        subPackets.put(new PlayerDataPacket());
        subPackets.put(new StopPacket());
        subPackets.put(new DiscoveryPacket());
        subPackets.put(new SongPacket());
        subPackets.put(new EmoteHeaderPacket());
        subPackets.put(new EmoteIconPacket());
    }

    //Write packet to a new ByteBuf
    public ByteBuffer write() throws IOException {
        if(data.purpose == PacketTask.UNKNOWN)throw new IllegalArgumentException("Can't send packet without any purpose...");
        AtomicReference<Byte> partCount = new AtomicReference<>((byte) 0);
        AtomicInteger sizeSum = new AtomicInteger(6); //5 bytes is the header
        subPackets.forEach((aByte, packet) -> {
            if(packet.doWrite(this.data)){
                if(!(packet instanceof SongPacket)){
                    partCount.getAndSet((byte) (partCount.get() + 1));
                    sizeSum.addAndGet(packet.calculateSize(this.data) + 6); //it's size + the header
                }
            }
        });
        if(sizeSum.get() > data.sizeLimit)throw new IOException("Can't send emote, packet's size is bigger than max allowed");
        SongPacket songPacket = (SongPacket) subPackets.get((byte)3);
        int songSize = songPacket.calculateSize(this.data) + 6;
        if(songPacket.doWrite(this.data) && sizeSum.get() + songSize <= data.sizeLimit){
            partCount.getAndSet((byte) (partCount.get() + 1));
            sizeSum.addAndGet(songSize);
        }
        else data.writeSong = false;

        ByteBuffer buf = ByteBuffer.allocate(sizeSum.get());

        buf.putInt(subPackets.get((byte)8).getVer(data.versions));
        buf.put(data.purpose.id);
        buf.put(partCount.get());

        AtomicBoolean ex = new AtomicBoolean(false);
        subPackets.forEach((aByte, packet) -> {
            try {
                writeSubPacket(buf, packet);
            } catch (IOException exception) {
                exception.printStackTrace();
                ex.set(true);
            }
        });
        if(ex.get())throw new IOException("Exception while writing sub-packages");
        return buf;
    }

    void writeSubPacket(ByteBuffer byteBuffer, AbstractNetworkPacket packetSender) throws IOException {
        if(packetSender.doWrite(this.data)){
            //This is not time critical task, HeapByteBuf is more secure and I can wrap it again.
            int len = packetSender.calculateSize(this.data);
            byteBuffer.put(packetSender.getID());
            byteBuffer.put(packetSender.getVer(data.versions));
            byteBuffer.putInt(len);
            int currentIndex = byteBuffer.position();
            packetSender.write(byteBuffer, this.data);
            if(byteBuffer.position() != currentIndex + len){
                throw new IOException("Incorrect size calculator: " + packetSender.getClass());
            }
        }
    }

    @Nullable
    public NetData read(ByteBuffer byteBuffer) throws IOException {

        try {
            this.version = byteBuffer.getInt();
            if (this.version > CommonData.networkingVersion) throw new IOException("Can't read newer version");
            data.purpose = PacketTask.getTaskFromID(byteBuffer.get());

            byte count = byteBuffer.get();

            for (int i = 0; i < count; i++) {
                byte id = byteBuffer.get();
                byte sub_version = byteBuffer.get();
                int size = byteBuffer.getInt();
                int currentPos = byteBuffer.position();
                if (subPackets.containsKey(id)) {
                    if(!subPackets.get(id).read(byteBuffer, this.data, sub_version)){
                        throw new IOException("Invalid " + subPackets.get(id).getClass().getName() + " sub-packet received");
                    }
                    if (byteBuffer.position() != size + currentPos) {
                        ((Buffer)byteBuffer).position(currentPos + size);
                    }
                }
                else {
                    ((Buffer)byteBuffer).position(currentPos + size);
                    //byteBuffer.position(currentPos + size);//Skip unknown sub-packets...
                }
            }

            if (data.prepareAndValidate()) return this.data;
            else return null;
        }
        catch (RuntimeException e){
            e.printStackTrace();
            throw new IOException(e.getClass().getTypeName() + " has occurred: " + e.getMessage());
        }
    }

    /**
     * EmotePacket builder.
     */
    public static class Builder{

        final NetData data;
        /**
         * To send an emote
         */
        public Builder setVersion(HashMap<Byte, Byte> versions){
            data.versions = versions;
            return this;
        }

        public NetData copyAndGetData(){
            return data.copy();
        }

        public Builder(NetData data){
            this.data = data;
        }

        public Builder copy(){
            return new Builder(this.data.copy());
        }

        public Builder(){
            data = new NetData();
        }

        public Builder setThreshold(float t){
            data.threshold = t;
            return this;
        }

        public EmotePacket build(){
            return new EmotePacket(data);
        }

        public EmotePacket build(int sizeLimit){
            return this.setSizeLimit(sizeLimit).build();
        }

        public Builder setSizeLimit(int sizeLimit){
            if(sizeLimit <= 0)throw new IllegalArgumentException("Size limit must be positive");
            data.sizeLimit = sizeLimit;
            return this;
        }

        public Builder configureToStreamEmote(KeyframeAnimation emoteData, @Nullable UUID player){
            if(data.purpose != PacketTask.UNKNOWN)throw new IllegalArgumentException("Can's send and stop emote at the same time");
            data.purpose = PacketTask.STREAM;
            data.emoteData = emoteData;
            data.player = player;
            return this;
        }

        public Builder configureToSaveEmote(KeyframeAnimation emoteData){
            if(data.purpose != PacketTask.UNKNOWN)throw new IllegalArgumentException("already configured?!");
            data.purpose = PacketTask.FILE;
            data.sizeLimit = Integer.MAX_VALUE;
            data.emoteData = emoteData;
            return this;
        }

        public Builder configureEmoteTick(int tick){
            this.data.tick = tick;
            return this;
        }

        public Builder configureTarget(@Nullable UUID target){
            data.player = target;
            return this;
        }

        public Builder configureToStreamEmote(KeyframeAnimation emoteData){
            return configureToStreamEmote(emoteData, null);
        }

        public Builder configureToSendStop(UUID emoteID, @Nullable UUID player){
            if(data.purpose != PacketTask.UNKNOWN)throw new IllegalArgumentException("Can't send emote and stop at the same time");
            data.purpose = PacketTask.STOP;
            data.stopEmoteID = emoteID;
            data.player = player;
            return this;
        }

        public Builder configureToSendStop(UUID emoteID){
            return configureToSendStop(emoteID, null);
        }

        public Builder configureToConfigExchange(boolean songEnabled){
            if(data.purpose != PacketTask.UNKNOWN)throw new IllegalArgumentException("Can't send config with emote or stop data...");
            data.purpose = PacketTask.CONFIG;
            HashMap<Byte, Byte> versions = new HashMap<>(EmotePacket.defaultVersions);
            if(!songEnabled){
                versions.replace((byte)3, (byte)0);
            }
            this.data.versions = versions;
            return this;
        }

        public void removePlayerID(){
            this.data.player = null;
        }

    }

}
