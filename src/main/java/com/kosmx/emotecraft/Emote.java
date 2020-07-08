package com.kosmx.emotecraft;

import com.kosmx.emotecraft.math.Ease;
import com.kosmx.emotecraft.math.Easing;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.List;

public class Emote {
    private boolean isRunning;
    private int currentTick;
    private int beginTick;
    private int endTick;
    private int resetTick;  //Tick after the emote ends, reset the pose to the normal minecraft pose...
    public Part head;
    public Torso torso;
    public Part rightArm;
    public Part leftArm;
    public Part leftLeg;
    public Part rightLeg;

    public Emote(int a, int b, int c){
        beginTick = a;
        endTick = b;
        resetTick = c;
    }

    private int lastPlayTick(){
        return endTick;
    }

    private int getCurrentTick(){
        return currentTick;
    }

    public void tick(){
        if(this.isRunning)this.currentTick++;
        if (currentTick > resetTick){
            this.isRunning = false;
        }
    }
    public static boolean isRunningEmote(@Nullable Emote emote){
        return (emote != null && emote.isRunning);
    }

    public void start(){
        this.currentTick = 0;
        this.isRunning = true;
        if (beginTick < 0)beginTick = 0;
        if (endTick < beginTick) endTick = beginTick;
        if (resetTick < endTick) resetTick = endTick;
    }

    private class BodyPart {
        public Part x;
        public Part y;
        public Part z;
        public RotationPart pitch;
        public RotationPart yaw;
        public RotationPart roll;

        public void setBodyPart(ModelPart modelPart, float tickDelta){
            modelPart.pivotX = x.getCurrentValue(modelPart.pivotX, tickDelta);
            modelPart.pivotY = y.getCurrentValue(modelPart.pivotY, tickDelta);
            modelPart.pivotZ = z.getCurrentValue(modelPart.pivotZ, tickDelta);
            modelPart.pitch = pitch.getCurrentValue(modelPart.pitch, tickDelta);
            modelPart.yaw = yaw.getCurrentValue(modelPart.yaw, tickDelta);
            modelPart.roll = roll.getCurrentValue(modelPart.roll, tickDelta);
        }
    }
    private class Torso extends BodyPart {
        public Vector3f getBodyOffshet(float tickDelta){
            float x = this.x.getCurrentValue(0, tickDelta);
            float y = this.y.getCurrentValue(0, tickDelta);
            float z = this.z.getCurrentValue(0, tickDelta);
            return new Vector3f(x, y, z);
        }
        public Vector3f getBodyRotation(float tickDelta){
            float x = this.pitch.getCurrentValue(0, tickDelta);
            float y = this.yaw.getCurrentValue(0, tickDelta);
            float z = this.roll.getCurrentValue(0, tickDelta);
            return new Vector3f(x, y, z); //TODO check the order!
        }
    }

    private class Part{
        private List<Move> list;

        /*
         *finds where is the current move
         */
        private int findTick(int tick) {
            int i = 0;
            while (this.list.size() > i+1 && this.list.get(i + 1).tick > tick) {
                i++;
            }
            return i;
        }
        public boolean add(Move move){
            return this.add(move, false);
        }
        protected boolean add(Move move, boolean sameTickException){
            int i = findTick(move.tick);
            if (!sameTickException && list.get(i).tick == move.tick || move.tick > lastPlayTick()){
                Main.log(Level.ERROR, "two moving at the same tick error", true);
                return false;
            }
            this.list.add(i, move);
            return true;
        }
        public float getCurrentValue(float currentState, float tickDelta){
            if(getCurrentTick()<beginTick) {    //if it is before playing the emote
                Move moveBefore = new Move(0, currentState, Ease.INOUTSINE);
                Move moveNext = list.get(0);
                return moveBefore.getPos(moveNext, tickDelta + currentTick);
            }
            else{
                Move nextMove;
                if (list.size() == 0) return currentState;
                int i = findTick(getCurrentTick());
                if (list.size() > i + 1) {
                    nextMove = list.get(i + 1);
                }
                else {
                    nextMove = new Move(resetTick, currentState, Ease.INOUTSINE);
                }
                return list.get(i).getPos(nextMove, tickDelta + getCurrentTick());
            }
        }

    }
    private class RotationPart extends Part{
        public boolean add(Move move, int rotate) {
            if( this.add(move)){
                this.add(new Move(move.tick,move.value + 6.28318530718f * rotate, move.ease),true);
                return true;
            }
            else return false;
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

        public float getPos(Move nextMove, float tickDelta){
            tickDelta = (tickDelta - (float)this.tick)/(nextMove.tick-this.tick);
            return MathHelper.lerp(Easing.easingFromEnum(this.ease, tickDelta), this.value, nextMove.value);
        }
    }
}
