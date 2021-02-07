package com.kosmx.emotecraft.network;

import com.kosmx.emotecraftCommon.EmoteData;
import com.kosmx.emotecraftCommon.math.Easing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.List;
import java.util.UUID;

/**
 * It should be placed into emotecraftCommon but it has too many references to minecraft codes...
 */
public class EmotePacket {
    protected EmoteData emote;
    protected UUID player;
    protected boolean valid = true;
    private int version = MainNetwork.networkingVersion;
    public boolean isRepeat = false;

    public EmotePacket(EmoteData emote, PlayerEntity playerEntity){
        this.emote = emote;
        player = playerEntity.getGameProfile().getId();
    }

    public EmotePacket(){
    }

    public boolean read(PacketByteBuf buf, boolean validate){
        this.version = buf.readInt();
        this.isRepeat = buf.readBoolean();
        player = buf.readUuid();    //we need to know WHO playings this emote
        //emote = new Emote(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt());
        emote = new EmoteData(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt());
        getBodyPartInfo(buf, emote.head, false);
        getBodyPartInfo(buf, emote.torso, true);
        getBodyPartInfo(buf, emote.rightArm, true);
        getBodyPartInfo(buf, emote.leftArm, true);
        getBodyPartInfo(buf, emote.rightLeg, true);
        getBodyPartInfo(buf, emote.leftLeg, true);
        return ! (!valid && validate) && emote.beginTick >= 0 && emote.beginTick < emote.endTick && (! emote.isInfinite || emote.returnToTick <= emote.endTick && emote.returnToTick >= 0);
    }

    public UUID getPlayer(){
        return this.player;
    }

    public EmoteData getEmote(){
        return emote;
    }

    public void write(PacketByteBuf buf, int version){
        this.version = version;
        buf.writeInt(version);
        buf.writeBoolean(isRepeat);
        buf.writeUuid(player);
        buf.writeInt(emote.beginTick);
        buf.writeInt(emote.endTick);
        buf.writeInt(emote.stopTick);
        buf.writeBoolean(emote.isInfinite);
        buf.writeInt(emote.returnToTick);
        writeBodyPartInfo(buf, emote.head, false);
        writeBodyPartInfo(buf, emote.torso, true);
        writeBodyPartInfo(buf, emote.rightArm, true);
        writeBodyPartInfo(buf, emote.leftArm, true);
        writeBodyPartInfo(buf, emote.rightLeg, true);
        writeBodyPartInfo(buf, emote.leftLeg, true);
    }

    private void writeBodyPartInfo(PacketByteBuf buf, EmoteData.StateCollection part, boolean bending){
        writePartInfo(buf, part.x);
        writePartInfo(buf, part.y);
        writePartInfo(buf, part.z);
        writePartInfo(buf, part.pitch);
        writePartInfo(buf, part.yaw);
        writePartInfo(buf, part.roll);
        if(bending) {
            writePartInfo(buf, part.bendDirection);
            writePartInfo(buf, part.bend);
        }
        else if(version < 4){
            writePartInfo(buf, EmoteData.EMPTY_STATE);
            writePartInfo(buf, EmoteData.EMPTY_STATE);
        }
    }

    private void writePartInfo(PacketByteBuf buf, EmoteData.StateCollection.State part){
        List<EmoteData.KeyFrame> list = part.keyFrames;
        buf.writeInt(list.size());
        for(EmoteData.KeyFrame move : list){
            buf.writeInt(move.tick);
            buf.writeFloat(move.value);
            buf.writeString(move.ease.toString());
        }
    }

    private void getBodyPartInfo(PacketByteBuf buf, EmoteData.StateCollection part, boolean bending){
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

    private void getPartInfo(PacketByteBuf buf, EmoteData.StateCollection.State part){
        int len = buf.readInt();
        for(int i = 0; i < len; i++){
            if(! part.addKeyFrame(buf.readInt(), buf.readFloat(), Easing.easeFromString(buf.readString(32767)))){
                this.valid = false;
            }
        }
    }
}
