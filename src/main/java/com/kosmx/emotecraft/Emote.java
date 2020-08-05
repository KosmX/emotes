package com.kosmx.emotecraft;

import com.kosmx.emotecraft.math.Ease;
import com.kosmx.emotecraft.math.Easing;
import com.kosmx.emotecraft.math.Helper;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Synchronisable, playable object
 *
 */
public class Emote {
    private boolean isRunning;
    private int currentTick;
    private int beginTick;
    private int endTick;
    private int stopTick;  //Tick after the emote ends, reset the pose to the normal minecraft pose...
    private boolean isInfinite = false;
    private boolean isInfStarted = false;
    private int returnTick;
    public BodyPart head = new BodyPart(0,0,0,0,0,0);
    public Torso torso = new Torso(0,0,0,0,0,0);
    public BodyPart rightArm = new BodyPart(-5,2,0,0,0,0.09f);
    public BodyPart leftArm = new BodyPart(5,2,0,0,0,-0.09f);
    public BodyPart leftLeg = new BodyPart(1.9f,12,0.1f,0,0,0);
    public BodyPart rightLeg = new BodyPart(-1.9f,12,0.1f,0,0,0);
    private float tickDelta = 0;
    private boolean isQuark = false;

    public Emote(int a, int b, int c, boolean inf, int returnTick){
        beginTick = a;
        endTick = b;
        stopTick = c;
        this.isInfinite = inf;
        this.returnTick = returnTick - 1;
    }
    public Emote(int a, int b, int c){
        beginTick = a;
        endTick = b;
        stopTick = c;
    }

    public Emote(int a){
        this.beginTick = a;
        this.isQuark = true;
    }

    public int getBeginTick(){
        return beginTick;
    }
    public int getEndTick(){
        return endTick;
    }
    public int getStopTick(){
        return stopTick;
    }
    public boolean isInfinite() {
        return isInfinite;
    }
    public int getReturnTick() {
        return returnTick;
    }

    public void setTickDelta(float f){
        this.tickDelta = f;
    }

    private int lastPlayTick(){
        return stopTick;
    }

    public float getCurrentTick(){
        return this.currentTick + this.tickDelta;
    }

    public void tick(){
        if(this.isRunning) this.currentTick++;
        else return;
        if(isInfinite && currentTick == endTick){
            currentTick = returnTick;
            isInfStarted = true;
        }
        if (currentTick > stopTick){
            this.isRunning = false;
        }
    }
    public static boolean isRunningEmote(@Nullable Emote emote){
        return (emote != null && emote.isRunning);
    }

    public void start(){
        this.currentTick = 0;
        this.isInfStarted = false;
        this.isRunning = true;
        if (beginTick < 0)beginTick = 0;
        if (endTick < beginTick) endTick = beginTick;
        if (stopTick <= endTick) stopTick = endTick + 1;
    }

    public void stop(){
        this.isRunning = false;
    }

    public static void addMove(Part part, int tick, float value, String easing, int turn, boolean degrees){
        part.add(tick, value, Easing.easeFromString(easing), turn, degrees);
    }
    public static boolean addMove(Part part, int tick, float value, String ease){
        return part.add(tick, value, Easing.easeFromString(ease));
    }

    public void setEndTick(int length) {
        this.endTick = length;
        if (endTick >= stopTick){
            this.stopTick = this.endTick + 1;
        }
    }

    public boolean isInfStarted() {
        return isInfStarted;
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
            float x = this.pitch.getCurrentValue(0, tickDelta);
            float y = this.yaw.getCurrentValue(0, tickDelta);
            float z = this.roll.getCurrentValue(0, tickDelta);
            return new Vector3f(x, y, z);
        }
    }

    public class Part{
        private final List<Move> list = new ArrayList<>();
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

        public List<Move> getList(){
            return list;
        }
        /*
         *finds where is the current move
         */
        public int findTick(int tick) {
            int i = -1;
            while (this.list.size() > i+1 && this.list.get(i + 1).tick <= tick) {
                i++;
            }
            return i;
        }
        public boolean add(int tick, float value, Ease ease, int rotate, boolean degrees){
            return this.add(new Move(tick, value, ease), false, true);
        }
        public boolean add(int tick, float value, Ease ease){
            return this.add(new Move(tick, value, ease), true, true);
        }
        protected boolean add(Move move, boolean sameTickException, boolean limit){
            //TODO add value limit
            int i = findTick(move.tick) + 1;
            if (this.list.size() != 0 && !sameTickException && this.list.get(i - 1).tick == move.tick || move.tick > lastPlayTick() && !isQuark){
                Main.log(Level.ERROR, "two moving at the same tick error");
            }
            if(limit && Math.abs(move.value) >= 20){
                Main.log(Level.WARN, "Invalid emote"); //TODO add weblink why...
                if(Main.config.validateEmote) return false;
            }
            this.list.add(i, move);
            return true;
        }

        private Move findBefore(int pos, float currentState){
            if(pos == -1){
                return (currentTick < beginTick || this.list.size() != 0) ? new Move(0, currentState, Ease.INOUTSINE)
                        : (currentTick < endTick) ? new Move(beginTick, defaultValue, Ease.INOUTSINE)
                        : new Move(endTick, defaultValue, Ease.INOUTSINE);
            }
            else {
                return  this.list.get(pos);
            }
        }
        private Move findAfter(int pos, float currentState){
            if(!(this.list.size() > pos +1)){
                return  (currentTick >= endTick || this.list.size() != 0) ? new Move(stopTick, currentState, Ease.INOUTSINE)
                        : (currentTick >= beginTick) ? new Move(endTick, defaultValue, Ease.INOUTSINE)
                        : new Move(beginTick, defaultValue, Ease.INOUTSINE);
            }
            else return this.list.get(pos + 1);
        }

        public float getCurrentValue(float currentState, float tickDelta){
            int pos = findTick(currentTick);
            Move moveBefore = findBefore(pos, currentState);
            if(isInfStarted && moveBefore.tick < returnTick){
                moveBefore = findBefore(findTick(endTick), currentState);
            }
            Move moveAfter = findAfter(pos, currentState);
            if(isInfinite && moveAfter.tick >= endTick){
                moveAfter = findAfter(findTick(returnTick), currentState);
            }
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
            if( this.add(new Move(tick, value, ease), false, false) && rotate != 0){
                this.add(new Move(tick, value + 6.28318530718f * rotate, ease), true, false);
                return true;
            }
            else return false;
        }

        @Override
        public float getCurrentValue(float currentState, float tickDelta) {
            return Helper.clamp(super.getCurrentValue(Helper.clamp(currentState), tickDelta));
        }

        @Override
        public boolean add(int tick, float value, Ease ease) {
            return this.add(new Move(tick, value, ease), true, false);
        }
    }

    public class Move{
        public int tick;
        public Float value;
        private Ease ease;

        public String getEase(){
            return ease.toString();
        }
        public void setEase(Ease ease){
            this.ease = ease;
        }

        public Move(int tick, float value, Ease ease){
            this.tick = tick;
            this.value = value;
            this.ease = ease;
        }
        public float getPos(Move nextMove, float tickDelta){
            //if(this == nextMove)return this.value;
            int tickBefore = this.tick;
            int tickAfter = nextMove.tick;
            if (this.tick >= nextMove.tick) {
                if(tickDelta < this.tick) tickBefore -= endTick - returnTick;
                else tickAfter += endTick - returnTick;
            }
            if(tickAfter == tickBefore)return this.value;
            tickDelta = (tickDelta - (float)tickBefore)/(tickAfter-tickBefore);
            return MathHelper.lerp(Easing.easingFromEnum(this.ease, tickDelta), this.value, nextMove.value);
        }
    }
}
