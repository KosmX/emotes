package com.kosmx.emotecraft;

import com.kosmx.emotecraft.math.Ease;
import com.kosmx.emotecraft.math.Easing;
import net.minecraft.client.model.ModelPart;

import java.util.List;

public class Emote {
    private int currentTick;
    private int lenght;
    public Part head;
    public Part torso;
    public Part rightArm;
    public Part leftArm;
    public Part leftLeg;
    public Part rightLeg;



    private class Part{
        public List<Move> x;
        public List<Move> y;
        public List<Move> z;
        public List<Move> pitch;
        public List<Move> yaw;
        public List<Move> roll;

        Part(ModelPart part){
            this.x.add(new Move(0, part.pivotX, Ease.INOUTSINE));
            this.y.add(new Move(0, part.pivotY, Ease.INOUTSINE));
            this.z.add(new Move(0, part.pivotZ, Ease.INOUTSINE));
            this.pitch.add(new Move(0, part.pitch, Ease.INOUTSINE));
            this.yaw.add(new Move(0, part.yaw, Ease.INOUTSINE));
            this.roll.add(new Move(0, part.roll, Ease.INOUTSINE));
        }
        
    }

    private class Move{
        public int tick;
        public Float value;
        private Ease ease;

        public Move(int tick, float value, Ease ease){
            this.tick = tick;
            this.value = value;
            this.ease = ease;
        }

        public float getPos(Move before, float tickDelta){
            //TODO
            return 0f;
        }
    }
}
