package io.github.kosmx.emotes.common.network;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.*;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMaps;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Send everything emotes mod data...
 */
public final class EmotePacket {
    public static final Byte2ByteMap defaultVersions;
    static {
        Byte2ByteOpenHashMap map = new Byte2ByteOpenHashMap();
        for (AbstractNetworkPacket packet : new AbstractNetworkPacket[] {
                new NewAnimPacket(), new EmoteDataPacket(), new PlayerDataPacket(),
                new DiscoveryPacket(), new StopPacket(), new SongPacket(),
                new EmoteHeaderPacket(), new EmoteIconPacket(), new RemoveEmotesPacket()
        }) {
            map.put(packet.getID(), packet.getVer());
        }
        defaultVersions = Byte2ByteMaps.unmodifiable(map);
    }

    private static final NetHashMap SUB_PACKETS = new NetHashMap(
            new DiscoveryPacket(),
            new NewAnimPacket(), new EmoteDataPacket(),
            new PlayerDataPacket(),
            new StopPacket(),
            new EmoteHeaderPacket(),
            new SongPacket(), new EmoteIconPacket(), new RemoveEmotesPacket()
    );

    public final NetData data;

    private EmotePacket(@NotNull NetData data) {
        if (data.versions.isEmpty()) data.versions.putAll(defaultVersions);
        this.data = data;
    }

    public EmotePacket(@NotNull ByteBuf byteBuf) {
        if (byteBuf.readInt() > CommonData.networkingVersion) throw new RuntimeException("Can't read newer version");
        this.data = new NetData();
        this.data.purpose = PacketTask.getTaskFromID(byteBuf.readByte());

        short count = byteBuf.readUnsignedByte();
        for (int i = 0; i < count; i++) {
            if (byteBuf.readableBytes() < 6) {
                throw new RuntimeException("Invalid sub-packet header");
            }

            AbstractNetworkPacket packet = SUB_PACKETS.get(byteBuf.readByte());
            byte subVersion = byteBuf.readByte();
            int size = byteBuf.readInt();
            int currentPos = byteBuf.readerIndex();

            if (size < 0 || size > byteBuf.readableBytes()) {
                throw new RuntimeException("Invalid sub-packet size: " + size);
            }

            if (packet != null) {
                try {
                    packet.read(byteBuf, this.data, subVersion);
                } catch (Throwable th) {
                    if (packet.isOptional()) {
                        CommonData.LOGGER.warn("Invalid {} sub-packet received!", packet, th);
                    } else {
                        throw new RuntimeException("Invalid " + packet + " sub-packet received", th);
                    }
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
     * Write packet to a ByteBuf.
     */
    public void write(ByteBuf buf) {
        if (data.purpose == PacketTask.UNKNOWN) throw new IllegalArgumentException("Can't send packet without any purpose...");

        int packetStart = buf.writerIndex();

        // Write header with placeholder count
        buf.writeInt(this.data.versions.getOrDefault(PacketConfig.DISCOVERY_PACKET, CommonData.networkingVersion));
        buf.writeByte(this.data.purpose.id);
        int countIndex = buf.writerIndex();
        buf.writeByte(0); // placeholder, filled in later

        int count = 0;
        try {
            for (AbstractNetworkPacket packet : SUB_PACKETS.values()) {
                if (!packet.doWrite(this.data)) continue;
                boolean optional = packet.isOptional();

                int subPacketStart = buf.writerIndex();
                try {
                    byte packetVersion = packet.getVer(this.data.versions);

                    buf.writeByte(packet.getID());
                    buf.writeByte(packetVersion);
                    int sizeIndex = buf.writerIndex();
                    buf.writeInt(0); // size placeholder

                    int contentStart = buf.writerIndex();
                    packet.write(buf, this.data, packetVersion);
                    int contentSize = buf.writerIndex() - contentStart;

                    // Fill in the real size
                    buf.setInt(sizeIndex, contentSize);

                    int totalSize = buf.writerIndex() - packetStart;
                    if (optional && totalSize > this.data.sizeLimit) {
                        // Rollback this sub-packet
                        buf.writerIndex(subPacketStart);
                        this.data.skippedPackets.add(packet.getID());
                        CommonData.LOGGER.warn("Writing {} skipped!", packet);
                        continue;
                    }

                    count++;
                } catch (IOException ex) {
                    buf.writerIndex(subPacketStart); // rollback on error
                    if (optional) {
                        CommonData.LOGGER.warn("Exception while writing {} sub-packet!", packet, ex);
                    } else {
                        throw ex;
                    }
                }
            }

            int sizeSum = buf.writerIndex() - packetStart;
            if (data.strictSizeLimit && sizeSum > data.sizeLimit) throw new RuntimeException(String.format(
                    "Can't send emote, packet's size (%s) is bigger than max allowed (%s)!", sizeSum, data.sizeLimit
            ));

            // Fill in the real count
            buf.setByte(countIndex, count);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * EmotePacket builder.
     */
    public record Builder(NetData data) {
        /**
         * To send an emote
         */
        public Builder setVersion(Map<Byte, Byte> versions) {
            data.versions.clear();
            data.versions.putAll(versions);
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
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("Can't send and stop emote at the same time");
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

        public Builder configureToRemoveEmote(Set<UUID> emoteIds) {
            if (data.purpose != PacketTask.UNKNOWN) throw new IllegalArgumentException("already configured?!");
            data.purpose = PacketTask.REMOVE;
            data.removeEmoteIds.addAll(emoteIds);
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
            setVersion(EmotePacket.defaultVersions);
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
