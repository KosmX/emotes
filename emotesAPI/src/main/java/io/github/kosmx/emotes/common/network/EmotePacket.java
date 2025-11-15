package io.github.kosmx.emotes.common.network;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Send everything emotes mod data...
 */
public final class EmotePacket {
    public static final Byte2ByteMap defaultVersions = new Byte2ByteOpenHashMap();
    private static final NetHashMap SUB_PACKETS = new NetHashMap();

    static {
        AbstractNetworkPacket tmp = new NewAnimPacket();
        defaultVersions.put(tmp.getID(), tmp.getVer());
        tmp = new EmoteDataPacket();
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

        SUB_PACKETS.put(new NewAnimPacket());
        SUB_PACKETS.put(new EmoteDataPacket());
        SUB_PACKETS.put(new PlayerDataPacket());
        SUB_PACKETS.put(new StopPacket());
        SUB_PACKETS.put(new DiscoveryPacket());
        SUB_PACKETS.put(new EmoteHeaderPacket());
        SUB_PACKETS.put(new SongPacket());
        SUB_PACKETS.put(new EmoteIconPacket());
    }

    public final NetData data;

    private EmotePacket(@NotNull NetData data) {
        if (data.versions == null || data.versions.isEmpty()) {
            data.versions = new HashMap<>(defaultVersions);
        }
        this.data = data;
    }

    public EmotePacket(@NotNull ByteBuf byteBuf) {
        if (byteBuf.readInt() > CommonData.networkingVersion) throw new RuntimeException("Can't read newer version");
        this.data = new NetData();
        this.data.purpose = PacketTask.getTaskFromID(byteBuf.readByte());

        byte count = byteBuf.readByte();
        for (int i = 0; i < count; i++) {
            AbstractNetworkPacket packet = SUB_PACKETS.get(byteBuf.readByte());
            byte subVersion = byteBuf.readByte();
            int size = byteBuf.readInt();
            int currentPos = byteBuf.readerIndex();

            if (packet != null) {
                try {
                    packet.read(byteBuf, this.data, subVersion);
                } catch (Throwable th) {
                    throw new RuntimeException("Invalid " + packet + " sub-packet received", th);
                }

                if (byteBuf.readerIndex() != size + currentPos) {
                    byteBuf.readerIndex(currentPos + size);
                }
            } else {
                byteBuf.readerIndex(currentPos + size);
            }
        }

        if (!data.prepareAndValidate()) throw new RuntimeException("no valid data");
    }

    /**
     * Write packet to a new ByteBuf
     */
    public void write(ByteBuf buf) {
        if (data.purpose == PacketTask.UNKNOWN) throw new IllegalArgumentException("Can't send packet without any purpose...");

        AtomicInteger sizeSum = new AtomicInteger(6); //5 bytes is the header // 5 bytes is the header

        Set<ByteBuf> writable = new HashSet<>();
        for (AbstractNetworkPacket packet : SUB_PACKETS.values()) {
            if (!packet.doWrite(this.data)) continue;
            boolean optional = packet.isOptional();

            ByteBuf packetBuff = null;
            try {
                packetBuff = writeSubPacket(packet);
            } catch (IOException ex) {
                if (optional) {
                    CommonData.LOGGER.warn("Exception while writing sub-package!", ex);
                } else {
                    throw new UncheckedIOException(ex);
                }
            }
            if (packetBuff == null) continue;

            int subPacketSize = packetBuff.writerIndex() + 6;
            if (!optional || (sizeSum.get() + subPacketSize) <= this.data.sizeLimit) {
                writable.add(packetBuff);
                sizeSum.addAndGet(subPacketSize);
            } else {
                this.data.skippedPackets.add(packet.getID());
                CommonData.LOGGER.warn("Writing {} skipped!", packet);
                packetBuff.release();
            }
        }
        if (data.strictSizeLimit && sizeSum.get() > data.sizeLimit) throw new RuntimeException(String.format(
                "Can't send emote, packet's size (%s) is bigger than max allowed (%s)!", sizeSum.get(), data.sizeLimit
        ));

        buf.writeInt(SUB_PACKETS.get(PacketConfig.DISCOVERY_PACKET).getVer(data.versions));
        buf.writeByte(this.data.purpose.id);
        buf.writeByte(writable.size());

        try {
            for (ByteBuf byteBuf : writable) {
                buf.writeBytes(byteBuf);
                byteBuf.release();
            }
        } catch (Throwable th) {
            throw new RuntimeException("Exception while writing sub-packages", th);
        }
    }

    private ByteBuf writeSubPacket(AbstractNetworkPacket packet) throws IOException {
        byte packetVersion = packet.getVer(this.data.versions);

        ByteBuf packetContent = Unpooled.buffer();
        try {
            packet.write(packetContent, this.data, packetVersion);

            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeByte(packet.getID());
            byteBuf.writeByte(packetVersion);
            byteBuf.writeInt(packetContent.writerIndex());
            byteBuf.writeBytes(packetContent);
            return byteBuf;
        } finally {
            packetContent.release();
        }
    }

    /**
     * EmotePacket builder.
     */
    public record Builder(NetData data) {
        /**
         * To send an emote
         */
        public Builder setVersion(HashMap<Byte, Byte> versions){
            data.versions = versions;
            return this;
        }

        @Override
        public NetData data() {
            return data.copy();
        }

        public Builder copy() {
            return new Builder(this.data.copy());
        }

        public Builder() {
            this(new NetData());
        }

        public EmotePacket build() {
            return new EmotePacket(data);
        }

        public EmotePacket build(int sizeLimit, boolean strict) {
            return this.setSizeLimit(sizeLimit, strict).build();
        }

        public Builder setSizeLimit(int sizeLimit, boolean strict) {
            if (sizeLimit <= 0) throw new IllegalArgumentException("Size limit must be positive");
            data.sizeLimit = sizeLimit;
            data.strictSizeLimit = strict;
            return this;
        }

        public Builder configureToStreamEmote(Animation emoteData, @Nullable UUID player) {
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("Can's send and stop emote at the same time");
            data.purpose = PacketTask.STREAM;
            data.emoteData = emoteData;
            data.player = player;
            return this;
        }

        public Builder configureToSaveEmote(Animation emoteData) {
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("already configured?!");
            data.purpose = PacketTask.FILE;
            data.sizeLimit = Integer.MAX_VALUE;
            data.emoteData = emoteData;
            return this;
        }

        public Builder configureEmoteTick(float tick) {
            this.data.tick = tick;
            return this;
        }

        public Builder configureTarget(@Nullable UUID target) {
            data.player = target;
            return this;
        }

        public Builder configureToStreamEmote(Animation emoteData) {
            return configureToStreamEmote(emoteData, null);
        }

        public Builder configureToSendStop(UUID emoteID, @Nullable UUID player) {
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("Can't send emote and stop at the same time");
            data.purpose = PacketTask.STOP;
            data.stopEmoteID = emoteID;
            data.player = player;
            return this;
        }

        public Builder configureToSendStop(UUID emoteID) {
            return configureToSendStop(emoteID, null);
        }

        public Builder configureToConfigExchange() {
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("Can't send config with emote or stop data...");
            this.data.purpose = PacketTask.CONFIG;
            this.data.versions = new HashMap<>(EmotePacket.defaultVersions);
            return this;
        }

        public void removePlayerID() {
            this.data.player = null;
        }

        public Builder strictSizeLimit(boolean strict) {
            data.strictSizeLimit = strict;
            return this;
        }
    }
}
