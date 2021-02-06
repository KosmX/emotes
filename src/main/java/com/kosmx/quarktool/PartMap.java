package com.kosmx.quarktool;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraftCommon.math.Ease;

public class PartMap {
    public Emote.BodyPart part;
    public PartValue x;
    public PartValue y;
    public PartValue z;

    public PartMap(Emote.BodyPart part){
        this.part = part;
        this.x = new PartValue(this.part.pitch);
        this.y = new PartValue(this.part.yaw);
        this.z = new PartValue(this.part.roll);
    }

    static class PartValue {
        private float value;
        private int lastTick;
        private final Emote.Part timeline;


        private PartValue(Emote.Part timeline){
            this.timeline = timeline;
        }

        public void addValue(int tick, float value, Ease ease){
            this.lastTick = tick;
            this.timeline.add(tick, value, ease);
        }

        public void addValue(int tickFrom, int tickTo, float value, Ease ease) throws QuarkParsingError{
            if(tickFrom < this.lastTick){
                throw new QuarkParsingError();
            }else if(tickFrom == this.lastTick && timeline.getList().size() != 0){
                timeline.getList().get(timeline.findTick(tickFrom)).setEase(ease);
            }else{
                timeline.add(tickFrom, this.value, ease);
            }
            this.value = value;
            this.lastTick = tickTo;
            this.timeline.add(tickTo, this.value, Ease.CONSTANT);
        }

        public float getValue(){
            return value;
        }

        public void hold(){
            this.timeline.getList().get(this.timeline.getList().size() - 1).setEase(Ease.CONSTANT);
        }

        public void setValue(float valueAfter){
            this.value = valueAfter;
        }
    }

}
