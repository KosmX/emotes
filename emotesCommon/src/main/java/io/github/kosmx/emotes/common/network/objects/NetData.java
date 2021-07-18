package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.opennbs.NBS;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
    public float threshold;
    @Nullable
    public AtomicInteger stopEmoteID = null;
    @Nullable
    public EmoteData emoteData = null;
    public int tick = 0;
    /**
     * Is the emote is valid (Not validated)
     */
    public boolean valid;
    //Never use it permanently
    @Nullable
    public NBS song = null;

    public boolean versionsUpdated = false;
    public HashMap<Byte, Byte> versions;

    //Set it to non-null if sending via MC Plugin channel
    //left it null when using Collar
    @Nullable
    public UUID player = null;

    public int sizeLimit = Short.MAX_VALUE;

    public boolean prepareAndValidate(){
        if(this.song != null){
            if(this.emoteData == null)return false;
            this.emoteData.song = this.song;
            this.song = null;
        }

        if(purpose == PacketTask.UNKNOWN)return false;
        if(purpose == PacketTask.STOP && stopEmoteID == null)return false;
        if(purpose == PacketTask.STREAM && emoteData == null)return false;
        if(purpose == PacketTask.CONFIG && !versionsUpdated)return false;
        if(emoteData != null && stopEmoteID != null)return false;
        //I won't simplify it because of readability
        return true;
    }

    public NetData copy() {
        NetData data = new NetData();
        data.purpose = this.purpose;
        data.threshold = threshold;
        data.stopEmoteID = stopEmoteID != null ? new AtomicInteger(stopEmoteID.get()) : null;
        data.emoteData = emoteData;
        data.tick = tick;
        data.valid = valid;
        data.song = song;
        data.versionsUpdated = versionsUpdated;
        data.versions = versions;
        data.player = player;
        data.sizeLimit = sizeLimit;
        return data;
    }
}
