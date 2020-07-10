package com.kosmx.emotecraft;

import com.kosmx.emotecraft.math.Ease;
import com.kosmx.emotecraft.math.Easing;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Emote {
    private boolean isRunning;
    private int currentTick;
    private int beginTick;
    private int endTick;
    private int stopTick;  //Tick after the emote ends, reset the pose to the normal minecraft pose...
    public BodyPart head = new BodyPart(0,0,0,0,0,0);
    public Torso torso = new Torso(0,0,0,0,0,0);
    public BodyPart rightArm = new BodyPart(-5,2,0,0,0,0.09f);
    public BodyPart leftArm = new BodyPart(5,2,0,0,0,-0.09f);
    public BodyPart leftLeg = new BodyPart(1.9f,12,0.1f,0,0,0);
    public BodyPart rightLeg = new BodyPart(-1.9f,12,0.1f,0,0,0);
    private float tickDelta = 0;

    public Emote(int a, int b, int c){
        beginTick = a;
        endTick = b;
        stopTick = c;
    }

    public void setTickDelta(float f){
        this.tickDelta = f;
    }

    private int lastPlayTick(){
        return stopTick;
    }

    private int getCurrentTickDelta(){
        return currentTick;
    }

    private float getCurrentTick(){
        return this.currentTick + this.tickDelta;
    }

    public void tick(){
        if(this.isRunning)this.currentTick++;
        if (currentTick > stopTick){
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
        if (stopTick <= endTick) stopTick = endTick + 1;
    }

    public void addMove(Part part, int tick, float value, String easing, int turn, boolean degrees){
        part.add(tick, value, Easing.easeFromString(easing), turn, degrees);
    }

    public class BodyPart {
        public Part x;
        public Part y;
        public Part z;
        public RotationPart pitch;
        public RotationPart yaw;
        public RotationPart roll;

        private BodyPart(float x, float y, float z, float yaw, float pitch, float roll){
            this.x = new Part(x);
            this.y = new Part(y);
            this.z = new Part(z);
            this.yaw = new RotationPart(yaw);
            this.pitch = new RotationPart(pitch);
            this.roll = new RotationPart(roll);
        }


        public void setBodyPart(ModelPart modelPart){
            modelPart.pivotX = x.getCurrentValue(modelPart.pivotX, tickDelta);
            modelPart.pivotY = y.getCurrentValue(modelPart.pivotY, tickDelta);
            modelPart.pivotZ = z.getCurrentValue(modelPart.pivotZ, tickDelta);
            modelPart.pitch = pitch.getCurrentValue(modelPart.pitch, tickDelta);
            modelPart.yaw = yaw.getCurrentValue(modelPart.yaw, tickDelta);
            modelPart.roll = roll.getCurrentValue(modelPart.roll, tickDelta);
        }
    }
    public class Torso extends BodyPart {
        private Torso(float x, float y, float z, float yaw, float pitch, float roll) {
            super(x, y, z, yaw, pitch, roll);
        }

        public Vec3d getBodyOffshet(){
            float x = this.x.getCurrentValue(0, tickDelta);
            float y = this.y.getCurrentValue(0, tickDelta);
            float z = this.z.getCurrentValue(0, tickDelta);
            return new Vec3d(x, y, z);
        }
        public Vector3f getBodyRotation(){
            float y = this.pitch.getCurrentValue(0, tickDelta);
            float x = this.yaw.getCurrentValue(0, tickDelta);
            float z = this.roll.getCurrentValue(0, tickDelta);
            return new Vector3f(x, y, z);
        }
    }

    public class Part{
        private final List<Move> list = new ArrayList<Move>();
        /*{
            @Override
            public Move get(int index){
                float tick = getCurrentTick() + tickDelta < beginTick ? beginTick : (tickDelta + getCurrentTick()) > endTick ? endTick : tickDelta + getCurrentTick();
                return (this.list.size() > index) ? super.get(index) : new Move(getCurrentTick(), defaultValue, Ease.INOUTSINE);
            }
        }
         */
        private final float defaultValue;

        private Part(float defaultValue){
            this.defaultValue = defaultValue;
        }

        /*
         *finds where is the current move
         */
        private int findTick(int tick) {
            int i = -1;
            while (this.list.size() > i+1 && this.list.get(i + 1).tick <= tick) {
                i++;
            }
            return i;
        }
        public boolean add(int tick, float value, Ease ease, int rotate, boolean degrees){
            return this.add(new Move(tick, value, ease), false);
        }
        protected boolean add(Move move, boolean sameTickException){
            //TODO add value limit
            int i = findTick(move.tick) + 1;
            if (this.list.size() != 0 && !sameTickException && this.list.get(i - 1).tick == move.tick || move.tick > lastPlayTick()){
                Main.log(Level.ERROR, "two moving at the same tick error", true);
                return false;
            }
            this.list.add(i, move);
            return true;
        }
        public float getCurrentValue(float currentState, float tickDelta){
            int pos = findTick(currentTick);
            Move moveBefore;
            Move moveAfter;
            if(pos == -1){
                moveBefore = (currentTick < beginTick || this.list.size() != 0) ? new Move(0, currentState, Ease.INOUTSINE)
                        : (currentTick < endTick) ? new Move(beginTick, defaultValue, Ease.INOUTSINE)
                        : new Move(endTick, defaultValue, Ease.INOUTSINE);
            }
            else {
                moveBefore = this.list.get(pos);
            }
            if(!(this.list.size() > pos +1)){
                    moveAfter = (currentTick >= endTick || this.list.size() != 0) ? new Move(stopTick, currentState, Ease.INOUTSINE)
                            : (currentTick >= beginTick) ? new Move(endTick, defaultValue, Ease.INOUTSINE)
                            : new Move(beginTick, defaultValue, Ease.INOUTSINE);
            }
            else moveAfter = this.list.get(pos + 1);
            return moveBefore.getPos(moveAfter, getCurrentTick());
        }

    }
    public class RotationPart extends Part{
        public RotationPart(float x) {
            super(x);
        }

        @Override
        public boolean add(int tick, float value, Ease ease, int rotate, boolean degrees) {
            if(degrees)value *= 0.01745329251f;
            if( this.add(new Move(tick, value, ease), false) && rotate != 0){
                this.add(new Move(tick, value + 6.28318530718f * rotate, ease), true);
                return true;
            }
            else return false;
        }
    }

    private static class Move{
        public int tick;
        public Float value;
        private final Ease ease;

        public Move(int tick, float value, Ease ease){
            this.tick = tick;
            this.value = value;
            this.ease = ease;
        }
        public Move(int tick, float value, String ease){
            this(tick, value, Easing.easeFromString(ease));
        }

        public float getPos(Move nextMove, float tickDelta){
            tickDelta = (tickDelta - (float)this.tick)/(nextMove.tick-this.tick);
            return MathHelper.lerp(Easing.easingFromEnum(this.ease, tickDelta), this.value, nextMove.value);
        }
    }
}
