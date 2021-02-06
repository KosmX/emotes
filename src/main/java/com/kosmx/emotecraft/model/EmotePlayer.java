package com.kosmx.emotecraft.model;


import com.kosmx.emotecraftCommon.EmoteData;
import com.kosmx.emotecraftCommon.math.Easing;
import jdk.internal.jline.internal.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Play emotes form a given EmoteData
 */
@Environment(EnvType.CLIENT)
public class EmotePlayer implements Tickable {
    private final EmoteData data;
    private boolean isRunning = true;
    private int currentTick = 0;
    private boolean isLoopStarted = false;

    protected float tickDelta;

    public BodyPart head;
    public Torso torso;
    public BodyPart rightArm;
    public BodyPart leftArm;
    public BodyPart rightLeg;
    public BodyPart leftLeg;

    /**
     *
     * @param emote EmoteData to play
     */
    public EmotePlayer(EmoteData emote){
        this.data = emote;
        head = new BodyPart(data.head);
        torso = new Torso(data.torso);
        rightArm = new BodyPart(data.rightArm);
        leftArm = new BodyPart(data.leftArm);
        rightLeg = new BodyPart(data.rightLeg);
        leftLeg = new BodyPart(data.leftLeg);
    }

    @Override
    public void tick(){
        if(this.isRunning){
            this.currentTick++;
            if(data.isInfinite && this.currentTick >= data.endTick){
                this.currentTick = data.returnToTick;
                this.isLoopStarted = true;
            }
            if(currentTick >= data.stopTick){
                this.isRunning = false;
            }
        }
    }

    /**
     * Is emotePlayer running
     * @param emote EmotePlayer, can be null
     * @return is running
     */
    public static boolean isRunningEmote(@Nullable EmotePlayer emote){
        return emote != null && emote.isRunning;
    }

    public void stop(){
        isRunning = false;
    }

    /**
     * is the emote already in an infinite loop?
     * @return :D
     */
    public boolean isLoopStarted(){
        return isLoopStarted;
    }

    public EmoteData getData() {
        return data;
    }

    public void setTickDelta(float tickDelta){
        this.tickDelta = tickDelta;
    }

    public class BodyPart{
        final EmoteData.StateCollection part;
        final Axis x;
        final Axis y;
        final Axis z;
        final RotationAxis pitch;
        final RotationAxis yaw;
        final RotationAxis roll;
        final RotationAxis bendAxis;
        final RotationAxis bend;


        public BodyPart(EmoteData.StateCollection part) {
            this.part = part;
            this.x = new Axis(part.x);
            this.y = new Axis(part.y);
            this.z = new Axis(part.z);
            this.pitch = new RotationAxis(part.pitch);
            this.yaw = new RotationAxis(part.yaw);
            this.roll = new RotationAxis(part.roll);
            this.bendAxis = new RotationAxis(part.bendDirection);
            this.bend = new RotationAxis(part.bend);
        }

        public void updateBodyPart(ModelPart modelPart){
            modelPart.pivotX = x.getValueAtCurrentTick(modelPart.pivotX);
            modelPart.pivotY = y.getValueAtCurrentTick(modelPart.pivotY);
            modelPart.pivotZ = z.getValueAtCurrentTick(modelPart.pivotZ);
            modelPart.pitch = pitch.getValueAtCurrentTick(modelPart.pitch);
            modelPart.yaw = yaw.getValueAtCurrentTick(modelPart.yaw);
            modelPart.roll = roll.getValueAtCurrentTick(modelPart.roll);
        }

        public Pair<Float, Float> getBend(){
            return new Pair<>(this.bendAxis.getValueAtCurrentTick(0), this.bend.getValueAtCurrentTick(0));
        }

    }

    public class Torso extends BodyPart{

        public Torso(EmoteData.StateCollection part) {
            super(part);
        }

        public Vec3d getBodyOffset(){
            float x = this.x.getValueAtCurrentTick(0);
            float y = this.y.getValueAtCurrentTick(0);
            float z = this.z.getValueAtCurrentTick(0);
            return new Vec3d(x, y, z);
        }

        public Vector3f getBodyRotation(){
            return new Vector3f(
                    this.pitch.getValueAtCurrentTick(0),
                    this.yaw.getValueAtCurrentTick(0),
                    this.roll.getValueAtCurrentTick(0)
            );
        }
    }

    public class Axis{
        protected final EmoteData.StateCollection.State keyframes;


        public Axis(EmoteData.StateCollection.State keyframes) {
            this.keyframes = keyframes;
        }

        private EmoteData.KeyFrame findBefore(int pos, float currentState){
            if(pos == -1){
                return (currentTick < data.beginTick || keyframes.length() != 0) ?
                        new EmoteData.KeyFrame(0, currentState) :
                        (currentTick < data.endTick) ?
                                new EmoteData.KeyFrame(data.beginTick, keyframes.defaultValue) :
                                new EmoteData.KeyFrame(data.endTick, keyframes.defaultValue);
            }
            return this.keyframes.keyFrames.get(pos);
        }

        private EmoteData.KeyFrame findAfter(int pos, float currentState){
            if(this.keyframes.length() > pos + 1){
                return this.keyframes.keyFrames.get(pos + 1);
            }
            return (currentState >= data.endTick || this.keyframes.length() != 0) && data.isInfinite ?
                    new EmoteData.KeyFrame(data.stopTick, currentState) :
                    (currentTick >= getData().beginTick) ?
                            new EmoteData.KeyFrame(getData().endTick, keyframes.defaultValue) :
                            new EmoteData.KeyFrame(getData().beginTick, keyframes.defaultValue);
        }

        /**
         * Get the current value of this axis.
         * @param currentValue the Current value of the axis
         * @return value
         */
        protected float getValueAtCurrentTick(float currentValue){
            int pos = keyframes.findAtTick(currentTick);
            EmoteData.KeyFrame keyBefore = findBefore(pos, currentValue);
            if(isLoopStarted && keyBefore.tick < data.returnToTick){
                keyBefore = findBefore(keyframes.findAtTick(data.returnToTick), currentValue);
            }
            EmoteData.KeyFrame keyAfter = findAfter(pos, currentValue);
            if(isLoopStarted && keyAfter.tick >= data.endTick){
                keyAfter = findAfter(keyframes.findAtTick(data.returnToTick - 1), currentValue);
            }
            return getValueFromKeyframes(keyBefore, keyAfter);
        }

        /**
         * Calculate the current value between keyframes
         * @param before Keyframe before
         * @param after Keyframe after
         * @return value
         */
        protected final float getValueFromKeyframes(EmoteData.KeyFrame before, EmoteData.KeyFrame after){
            int tickBefore = before.tick;
            int tickAfter = after.tick;
            if(tickBefore >= tickAfter){
                if(currentTick < tickBefore) tickBefore -= data.endTick - data.returnToTick;
                else tickAfter += data.endTick - data.returnToTick;
            }
            if(tickBefore == tickAfter) return before.value;
            float f = (currentTick + tickDelta - (float) tickBefore) / (tickAfter - tickBefore);
            return MathHelper.lerp(Easing.easingFromEnum(before.ease, tickDelta), before.value, after.value);
        }

    }
    public class RotationAxis extends Axis{

        public RotationAxis(EmoteData.StateCollection.State keyframes) {
            super(keyframes);
        }

        @Override
        protected float getValueAtCurrentTick(float currentValue) {
            return super.getValueAtCurrentTick(currentValue)%(float) Math.PI;
        }
    }
}
