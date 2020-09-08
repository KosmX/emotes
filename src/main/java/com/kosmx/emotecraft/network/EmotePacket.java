package com.kosmx.emotecraft.network;

import com.kosmx.emotecraft.Emote;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.List;
import java.util.UUID;

public class EmotePacket{
    protected Emote emote;
    protected UUID player;
    protected boolean correct = true;
    private int version = 2;
    public boolean isRepeat = false;

    public EmotePacket(Emote emote, PlayerEntity playerEntity){
        this.emote = emote;
        player = playerEntity.getGameProfile().getId();
    }
    public EmotePacket(){}

    //TODO sync bending

    public boolean read(PacketByteBuf buf, boolean validate){
        this.version = buf.readInt();
        this.isRepeat = buf.readBoolean();
        player = buf.readUuid();    //we need to know WHO playings this emote
        emote = new Emote(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt());
        getBodyPartInfo(buf, emote.head);
        getBodyPartInfo(buf, emote.torso);
        getBodyPartInfo(buf, emote.rightArm);
        getBodyPartInfo(buf, emote.leftArm);
        getBodyPartInfo(buf, emote.rightLeg);
        getBodyPartInfo(buf, emote.leftLeg);
        return !(!correct && validate) && emote.getBeginTick() >= 0 && emote.getBeginTick() < emote.getEndTick() && (!emote.isInfinite() || emote.getReturnTick() < emote.getEndTick() && emote.getReturnTick() >= 0);
    }
    public UUID getPlayer(){
        return this.player;
    }
    public Emote getEmote(){
        return emote;
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(version);
        buf.writeBoolean(isRepeat);
        buf.writeUuid(player);
        buf.writeInt(emote.getBeginTick());
        buf.writeInt(emote.getEndTick());
        buf.writeInt(emote.getStopTick());
        buf.writeBoolean(emote.isInfinite());
        buf.writeInt(emote.getReturnTick());
        writeBodyPartInfo(buf, emote.head);
        writeBodyPartInfo(buf, emote.torso);
        writeBodyPartInfo(buf, emote.rightArm);
        writeBodyPartInfo(buf, emote.leftArm);
        writeBodyPartInfo(buf, emote.rightLeg);
        writeBodyPartInfo(buf, emote.leftLeg);
    }
    private void writeBodyPartInfo(PacketByteBuf buf, Emote.BodyPart part){
        writePartInfo(buf, part.x);
        writePartInfo(buf, part.y);
        writePartInfo(buf, part.z);
        writePartInfo(buf, part.pitch);
        writePartInfo(buf, part.yaw);
        writePartInfo(buf, part.roll);
    }
    private void writePartInfo(PacketByteBuf buf, Emote.Part part){
        List<Emote.Move> list = part.getList();
        buf.writeInt(list.size());
        for (Emote.Move move : list) {
            buf.writeInt(move.tick);
            buf.writeFloat(move.value);
            buf.writeString(move.getEase());
        }
    }

    private void getBodyPartInfo(PacketByteBuf buf, Emote.BodyPart part){
        getPartInfo(buf, part.x);
        getPartInfo(buf, part.y);
        getPartInfo(buf, part.z);
        getPartInfo(buf, part.pitch);
        getPartInfo(buf, part.yaw);
        getPartInfo(buf, part.roll);
    }
    private void getPartInfo(PacketByteBuf buf, Emote.Part part) {
        int len = buf.readInt();
        for(int i = 0; i<len; i++){
            if(!Emote.addMove(part, buf.readInt(), buf.readFloat(), buf.readString(32767))){
                this.correct = false;
            }
        }
    }
}
