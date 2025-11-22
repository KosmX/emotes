package io.github.kosmx.emotes.common.network.objects;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.PacketTask;

import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * It won't be public until a success read.....
 */
public final class NetData {
    /**
     * 0 - none, invalid
     * 1 - stream emote
     * 8 - config exchange
     * 10 - stop
     * //as the sub-packet ids
     */
    public PacketTask purpose = PacketTask.UNKNOWN;
    @Nullable
    public UUID stopEmoteID = null;
    @Nullable
    public Animation emoteData = null;
    public float tick = 0;
    /**
     * Is the emote is valid (Not validated)
     */
    public boolean valid;
    //Never use it permanently

    public final ByteSet skippedPackets = new ByteOpenHashSet();

    public boolean versionsUpdated = false;
    public final Map<Byte, Byte> versions = new HashMap<>();

    //Set it to non-null if sending via MC Plugin channel
    //left it null when using Collar
    @Nullable
    public UUID player = null;
    //Forced flag
    //On play, it can not be stopped by the player
    //On stop, the server stops it not because invalid but because event stopped it
    public boolean isForced = false;

    /**
     * net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket#MAX_PAYLOAD_SIZE
     */
    public int sizeLimit = CommonData.MAX_PACKET_SIZE;
    public boolean strictSizeLimit = true;

    final Map<String, Object> extraData = new HashMap<>();

    public boolean prepareAndValidate() {
        if (this.emoteData != null && !this.extraData.isEmpty()) {
            emoteData.data().data().putAll(extraData);
        }

        if(purpose == PacketTask.UNKNOWN)return false;
        if(purpose == PacketTask.STOP && stopEmoteID == null)return false;
        if(purpose == PacketTask.STREAM && emoteData == null)return false;
        if(purpose == PacketTask.CONFIG && !versionsUpdated)return false;
        return emoteData == null || stopEmoteID == null;
        //I won't simplify it because of readability
    }

    public NetData copy() {
        NetData data = new NetData();
        data.purpose = this.purpose;
        data.stopEmoteID = stopEmoteID;
        data.emoteData = emoteData;
        data.tick = tick;
        data.valid = valid;
        data.versionsUpdated = versionsUpdated;
        data.versions.putAll(versions);
        data.player = player;
        data.sizeLimit = sizeLimit;
        data.isForced = isForced;
        return data;
    }

    @Override
    public String toString() {
        return "NetData{" +
                "purpose=" + purpose +
                ", stopEmoteID=" + stopEmoteID +
                ", emoteData=" + emoteData +
                ", startingAt=" + tick +
                ", player=" + player +
                '}';
    }
}
