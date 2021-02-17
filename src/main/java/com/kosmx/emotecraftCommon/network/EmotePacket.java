package com.kosmx.emotecraftCommon.network;

import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraftCommon.EmoteData;
import com.kosmx.emotecraftCommon.math.Easing;
import com.kosmx.emotecraftCommon.opennbs.NBS;
import com.kosmx.emotecraftCommon.opennbs.network.NBSPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.UUID;

/**
 * It should be placed into emotecraftCommon but it has too many references to minecraft codes...
 */
public class EmotePacket {
    protected EmoteData emote;
    protected UUID player;
    protected boolean valid = true;
    private int version;
    public boolean isRepeat = false;

    public EmotePacket(EmoteData emote, UUID playerEntity){
        this.emote = emote;
        player = playerEntity;
    }

    public EmotePacket(){
    }

    public boolean read(ByteBuf buf, float validationThreshold){
        this.version = buf.readInt();
        this.isRepeat = buf.readBoolean();
        player = CommonNetwork.readUUID(buf);    //we need to know WHO playings this emote
        //emote = new Emote(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt());
        EmoteData.EmoteBuilder builder = new EmoteData.EmoteBuilder();
        builder.validationThreshold = validationThreshold;
        builder.beginTick = buf.readInt();
        builder.endTick = buf.readInt();
        builder.stopTick = buf.readInt();
        builder.isLooped = buf.readBoolean();
        builder.returnTick = buf.readInt();
        if(version >= 5){
            builder.isEasingBefore = buf.readBoolean();
        }
        //emote = new EmoteData(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt(), validationThreshold);
        getBodyPartInfo(buf, builder.head, false);
        getBodyPartInfo(buf, builder.torso, true);
        getBodyPartInfo(buf, builder.rightArm, true);
        getBodyPartInfo(buf, builder.leftArm, true);
        getBodyPartInfo(buf, builder.rightLeg, true);
        getBodyPartInfo(buf, builder.leftLeg, true);

        emote = builder.build();
        if(version >= 6){
            boolean sound = buf.readBoolean();
            if(sound){
                NBSPacket nbsPacket = new NBSPacket();
                nbsPacket.read(buf);
                emote.song = nbsPacket.getSong();
            }
        }

        return valid && emote.beginTick >= 0 && emote.beginTick < emote.endTick && (! emote.isInfinite || emote.returnToTick <= emote.endTick && emote.returnToTick >= 0);
    }

    public UUID getPlayer(){
        return this.player;
    }

    public EmoteData getEmote(){
        return emote;
    }

    public void write(ByteBuf buf, int version){
        this.version = Math.min(version, CommonData.networkingVersion);
        buf.writeInt(this.version);
        buf.writeBoolean(isRepeat);
        CommonNetwork.writeUUID(buf, player);
        buf.writeInt(emote.beginTick);
        buf.writeInt(emote.endTick);
        buf.writeInt(emote.stopTick);
        buf.writeBoolean(emote.isInfinite);
        buf.writeInt(emote.returnToTick);
        if(this.version >= 5){
            buf.writeBoolean(emote.isEasingBefore);
        }
        writeBodyPartInfo(buf, emote.head, false, emote);
        writeBodyPartInfo(buf, emote.torso, true, emote);
        writeBodyPartInfo(buf, emote.rightArm, true, emote);
        writeBodyPartInfo(buf, emote.leftArm, true, emote);
        writeBodyPartInfo(buf, emote.rightLeg, true, emote);
        writeBodyPartInfo(buf, emote.leftLeg, true, emote);

        //Just make the NBS streaming junk
        if(version >= 6) {
            buf.writeBoolean(emote.song != null);
            if (emote.song != null) {
                NBSPacket nbsPacket = new NBSPacket(emote.song);
                nbsPacket.write(buf); //It will reworked :D
            }
        }
    }

    private void writeBodyPartInfo(ByteBuf buf, EmoteData.StateCollection part, boolean bending, EmoteData emoteData){
        writePartInfo(buf, part.x, emoteData);
        writePartInfo(buf, part.y, emoteData);
        writePartInfo(buf, part.z, emoteData);
        writePartInfo(buf, part.pitch, emoteData);
        writePartInfo(buf, part.yaw, emoteData);
        writePartInfo(buf, part.roll, emoteData);
        if(bending) {
            writePartInfo(buf, part.bendDirection, emoteData);
            writePartInfo(buf, part.bend, emoteData);
        }
        else if(version < 4){
            writePartInfo(buf, EmoteData.EMPTY_STATE, emoteData);
            writePartInfo(buf, EmoteData.EMPTY_STATE, emoteData);
        }
    }

    private void writePartInfo(ByteBuf buf, EmoteData.StateCollection.State part, EmoteData emoteData){
        List<EmoteData.KeyFrame> list = part.keyFrames;
        buf.writeInt(list.size());
        for(EmoteData.KeyFrame move : list){
            buf.writeInt(move.tick);
            buf.writeFloat(move.value);
            if(this.version < 5 && emoteData.isEasingBefore){
                if(list.indexOf(move) + 1 < list.size()){
                    CommonNetwork.writeVarString(buf, list.get(list.indexOf(move) + 1).ease.toString()); //try to back-convert emotes
                }
                else {
                    CommonNetwork.writeVarString(buf, move.ease.toString());
                }
            }
            else CommonNetwork.writeVarString(buf, move.ease.toString());
        }
    }

    private void getBodyPartInfo(ByteBuf buf, EmoteData.StateCollection part, boolean bending){
        getPartInfo(buf, part.x);
        getPartInfo(buf, part.y);
        getPartInfo(buf, part.z);
        getPartInfo(buf, part.pitch);
        getPartInfo(buf, part.yaw);
        getPartInfo(buf, part.roll);
        if(bending || version < 4) {
            getPartInfo(buf, part.bendDirection);
            getPartInfo(buf, part.bend);
        }
    }

    private void getPartInfo(ByteBuf buf, EmoteData.StateCollection.State part){
        int len = buf.readInt();
        for(int i = 0; i < len; i++){
            if(! part.addKeyFrame(buf.readInt(), buf.readFloat(), Easing.easeFromString(CommonNetwork.readVarString(buf)))){
                this.valid = false;
            }
        }
    }
}
