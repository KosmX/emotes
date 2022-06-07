package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.network.PacketTask;

import javax.annotation.Nullable;
import java.util.HashMap;
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
    public float threshold;
    @Nullable
    public UUID stopEmoteID = null;
    @Nullable
    public EmoteData emoteData = null;
    private EmoteData.EmoteBuilder emoteBuilder = null;
    public int tick = 0;
    /**
     * Is the emote is valid (Not validated)
     */
    public boolean valid;
    //Never use it permanently

    public boolean wasEmoteData = false;
    public boolean writeSong = true;

    public boolean versionsUpdated = false;
    public HashMap<Byte, Byte> versions;

    //Set it to non-null if sending via MC Plugin channel
    //left it null when using Collar
    @Nullable
    public UUID player = null;
    //Forced flag
    //On play, it can not be stopped by the player
    //On stop, the server stops it not because invalid but because event stopped it
    public boolean isForced = false;

    public int sizeLimit = Short.MAX_VALUE;

    public EmoteData.EmoteBuilder getEmoteBuilder(){
        if(emoteBuilder == null){
             emoteBuilder = new EmoteData.EmoteBuilder(threshold, EmoteFormat.BINARY);
        }
        return emoteBuilder;
    }

    public boolean prepareAndValidate(){
        if(emoteBuilder != null) {
            if(emoteData != null) return false;
            if(!wasEmoteData)return false;
            emoteData = emoteBuilder.build();
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
        data.stopEmoteID = stopEmoteID;
        data.emoteData = emoteData;
        data.tick = tick;
        data.valid = valid;
        data.versionsUpdated = versionsUpdated;
        data.versions = versions;
        data.player = player;
        data.sizeLimit = sizeLimit;
        data.isForced = isForced;
        return data;
    }

    @Override
    public String toString() {
        return "NetData{" +
                "purpose=" + purpose +
                ", threshold=" + threshold +
                ", stopEmoteID=" + stopEmoteID +
                ", emoteData=" + emoteData +
                ", startingAt=" + tick +
                ", player=" + player +
                '}';
    }
}
