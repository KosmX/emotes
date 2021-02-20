package com.kosmx.emotes.common.network.objects;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.opennbs.NBS;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

/*
 * It won't be public until a success read.....
 */
public class NetData {
    public float threshold;
    @Nullable
    public UUID stopEmoteID = null;
    @Nullable
    public EmoteData emoteData = null;
    public int tick = 0;
    public boolean valid;
    @Nullable
    public NBS song = null;

    public boolean versionsUpdated = false;
    public HashMap<Byte, Byte> versions;

    //Set it to non-null if sending via MC Plugin channel
    //left it null when using Collar
    @Nullable
    public UUID player = null;

    public int sizeLimit = Short.MAX_VALUE;

    public boolean isValid(){
        if(emoteData == null && !versionsUpdated && stopEmoteID == null)return false;
        if(emoteData != null && stopEmoteID != null)return false;
        //I won't simplify it because of readability
        return true;
    }

}
