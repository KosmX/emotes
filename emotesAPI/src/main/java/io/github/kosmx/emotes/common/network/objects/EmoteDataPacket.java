package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Ease;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

/**
 * It should be placed into emotecraftCommon but it has too many references to minecraft codes...
 */
public class EmoteDataPacket extends AbstractNetworkPacket {
    protected boolean valid = true;
    private int version;
    public int tick = 0;

    byte keyframeSize = 9;

    public EmoteDataPacket(){
    }

    @Override
    public void write(ByteBuffer buf, NetData config){
        this.version = Math.min(config.versions.get(getVer()), CommonData.networkingVersion);
        EmoteData emote = config.emoteData;
        buf.putInt(config.tick);
        buf.putInt(emote.beginTick);
        buf.putInt(emote.endTick);
        buf.putInt(emote.stopTick);
        putBoolean(buf, emote.isInfinite);
        buf.putInt(emote.returnToTick);
        putBoolean(buf, emote.isEasingBefore);
        putBoolean(buf, emote.nsfw);
        buf.put(keyframeSize);
        writeBodyPartInfo(buf, emote.head);
        writeBodyPartInfo(buf, emote.body);
        writeBodyPartInfo(buf, emote.rightArm);
        writeBodyPartInfo(buf, emote.leftArm);
        writeBodyPartInfo(buf, emote.rightLeg);
        writeBodyPartInfo(buf, emote.leftLeg);
        buf.putLong(config.emoteData.getUuid().getMostSignificantBits());
        buf.putLong(config.emoteData.getUuid().getLeastSignificantBits());
    }

    private void writeBodyPartInfo(ByteBuffer buf, EmoteData.StateCollection part){
        writePartInfo(buf, part.x);
        writePartInfo(buf, part.y);
        writePartInfo(buf, part.z);
        writePartInfo(buf, part.pitch);
        writePartInfo(buf, part.yaw);
        writePartInfo(buf, part.roll);
        if(part.isBendable) {
            writePartInfo(buf, part.bendDirection);
            writePartInfo(buf, part.bend);
        }
    }

    private void writePartInfo(ByteBuffer buf, EmoteData.StateCollection.State part){
        List<EmoteData.KeyFrame> list = part.keyFrames;
        buf.putInt(part.isEnabled ? list.size() : -1);
        if(part.isEnabled) {
            for (EmoteData.KeyFrame move : list) {
                buf.putInt(move.tick);
                buf.putFloat(move.value);
                buf.put(move.ease.getId());
            }
        }
    }

    @Override
    public boolean read(ByteBuffer buf, NetData config, int version) throws IOException {
        this.version = version;
        EmoteData.EmoteBuilder builder = config.getEmoteBuilder();
        config.tick = buf.getInt();
        builder.beginTick = buf.getInt();
        builder.endTick = buf.getInt();
        builder.stopTick = buf.getInt();
        builder.isLooped = getBoolean(buf);
        builder.returnTick = buf.getInt();
        builder.isEasingBefore = getBoolean(buf);
        builder.nsfw = getBoolean(buf);
        keyframeSize = buf.get();
        if(!(keyframeSize > 0)) throw new IOException("keyframe size must be greater than 0, current: " + keyframeSize);
        getBodyPartInfo(buf, builder.head, false);
        getBodyPartInfo(buf, builder.body, true);
        getBodyPartInfo(buf, builder.rightArm, true);
        getBodyPartInfo(buf, builder.leftArm, true);
        getBodyPartInfo(buf, builder.rightLeg, true);
        getBodyPartInfo(buf, builder.leftLeg, true);
        if(version >= 1){
            long msb = buf.getLong();
            long lsb = buf.getLong();
            builder.uuid = new UUID(msb, lsb);
        }

        //EmoteData emote = builder.build();
        boolean correct = builder.beginTick >= 0 && builder.beginTick < builder.endTick && (! builder.isLooped || builder.returnTick <= builder.endTick && builder.returnTick >= 0);
        valid = valid && correct;

        config.valid = valid;

        config.wasEmoteData = true;
        return correct;
    }

    private void getBodyPartInfo(ByteBuffer buf, EmoteData.StateCollection part, boolean bending){
        getPartInfo(buf, part.x);
        getPartInfo(buf, part.y);
        getPartInfo(buf, part.z);
        getPartInfo(buf, part.pitch);
        getPartInfo(buf, part.yaw);
        getPartInfo(buf, part.roll);
        if(bending) {
            getPartInfo(buf, part.bendDirection);
            getPartInfo(buf, part.bend);
        }
    }

    private void getPartInfo(ByteBuffer buf, EmoteData.StateCollection.State part){
        int len = buf.getInt();
        part.isEnabled = len != -1;
        for(int i = 0; i < len; i++){
            int currentPos = buf.position();
            if(! part.addKeyFrame(buf.getInt(), buf.getFloat(), Ease.getEase(buf.get()))){
                this.valid = false;
            }
            ((Buffer)buf).position(currentPos + keyframeSize);
            //ByteBuffer#position(I)V;Buffer in Java 1.8 but
            //ByteBuffer#position(I)V;ByteBuffer in later versions
        }
    }

    @Override
    public byte getID() {
        return 0;
    }

    @Override
    public byte getVer() {
        /**
         * version 1: 2.1 features, extended parts, UUID emote ID
         */
        return 1;
    }

    @Override
    public boolean doWrite(NetData data) {
        return data.emoteData != null && data.stopEmoteID == null;
    }

    /*
    Data types in comment:
    I int, 4 bytes
    L Long 8 bytes (1 uuid = 2 L)
    B byte, ...1 byte
    F float, 4 bytes
     */
    @Override
    public int calculateSize(NetData config) {
        if(config.emoteData == null)return 0;
        //I will create less efficient loops but these will be more easily fixable
        int size = 24;//The header makes 46 bytes IIIIBIBBB
        size += partSize(config.emoteData.head);
        size += partSize(config.emoteData.body);
        size += partSize(config.emoteData.rightArm);
        size += partSize(config.emoteData.leftArm);
        size += partSize(config.emoteData.rightLeg);
        size += partSize(config.emoteData.leftLeg);
        //The size of an empty emote is 230 bytes.
        //but that makes the size to be 230 + keyframes count*9 bytes.
        //46 axis, including bends for every body-part except head.
        return size;
    }

    int partSize(EmoteData.StateCollection part){
        int size = 0;
        size += axisSize(part.x);
        size += axisSize(part.y);
        size += axisSize(part.z);
        size += axisSize(part.pitch);
        size += axisSize(part.yaw);
        size += axisSize(part.roll);
        if(part.isBendable) {
            size += axisSize(part.bend);
            size += axisSize(part.bendDirection);
        }
        return size;
    }
    int axisSize(EmoteData.StateCollection.State axis){
        return axis.keyFrames.size()*keyframeSize + 4;// count*IFB + I (for count)
    }
}
