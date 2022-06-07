package io.github.kosmx.playerAnim.layered;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Easing;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.emotes.common.tools.Vector3;
import io.github.kosmx.playerAnim.TransformType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation player for EmoteX emote format,
 * It does not mean, you can not use it, It means Emotecraft uses this too!
 */
public class EmoteDataPlayer implements IAnimation {



    private final EmoteData data;
    private boolean isRunning = true;
    private int currentTick = 0;
    private boolean isLoopStarted = false;

    protected float tickDelta;

    /**
     * Will be removed when I give up the 1.16- support
     */
    public final HashMap<String, BodyPart> bodyParts;
    public int perspective = 0;

    /**
     *
     * @param emote emote to play
     * @param t begin playing from tick
     */
    public EmoteDataPlayer(EmoteData emote, int t) {
        this.data = emote;

        this.bodyParts = new HashMap<>(emote.bodyParts.size());
        for(Map.Entry<String, EmoteData.StateCollection> part:emote.bodyParts.entrySet()){
            this.bodyParts.put(part.getKey(), new BodyPart(part.getValue()));
        }

        this.currentTick = t;
        if(isInfinite() && t > data.returnToTick){
            currentTick = (t - data.returnToTick)%(data.endTick- data.returnToTick) + data.returnToTick;
        }
    }

    @Override
    public void tick() {
        if (this.isRunning) {
            this.currentTick++;
            if (data.isInfinite && this.currentTick >= data.endTick) {
                this.currentTick = data.returnToTick;
                this.isLoopStarted = true;
            }
            if (currentTick >= data.stopTick) {
                this.stop();
            }
        }
    }

    public int getTick() {
        return this.currentTick;
    }


    public void stop() {
        isRunning = false;
        //if(this.perspectiveRedux) PerspectiveReduxProxy.setPerspective(false);
        //if(this.perspective != null) MinecraftClient.getInstance().options.setPerspective(perspective); //TODO
    }

    @Override
    public boolean isActive() {
        return this.isRunning;
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        BodyPart part = bodyParts.get(modelName);
        if (part == null) return value0;
        switch (type) {
            case POSITION:
                return part.getBodyOffset(value0);
            case ROTATION:
                Vector3<Float> rot = part.getBodyRotation(value0);
                return new Vec3f(rot.getX(), rot.getY(), rot.getZ());
            case BEND:
                Pair<Float, Float> bend = part.getBend(new Pair<>(value0.getX(), value0.getY()));
                return new Vec3f(bend.getLeft(), bend.getRight(), 0f);
            default:
                return value0;
        }
    }

    @Override
    public void setupAnim(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    /**
     * is the emote already in an infinite loop?
     *
     * @return :D
     */
    public boolean isLoopStarted() {
        return isLoopStarted;
    }


    public EmoteData getData() {
        return data;
    }

    public BodyPart getPart(String string){
        BodyPart part = bodyParts.get(string);
        return part == null ? new BodyPart(null) : part;
    }


    public int getStopTick() {
        return this.data.stopTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public boolean isInfinite() {
        return data.isInfinite;
    }


    public class BodyPart {
        @Nullable
        public final EmoteData.StateCollection part;
        public final Axis x;
        public final Axis y;
        public final Axis z;
        public final RotationAxis pitch;
        public final RotationAxis yaw;
        public final RotationAxis roll;
        public final RotationAxis bendAxis;
        public final RotationAxis bend;


        public BodyPart(@Nullable EmoteData.StateCollection part) {
            this.part = part;
            if(part != null) {
                this.x = new Axis(part.x);
                this.y = new Axis(part.y);
                this.z = new Axis(part.z);
                this.pitch = new RotationAxis(part.pitch);
                this.yaw = new RotationAxis(part.yaw);
                this.roll = new RotationAxis(part.roll);
                this.bendAxis = new RotationAxis(part.bendDirection);
                this.bend = new RotationAxis(part.bend);
            }
            else {
                this.x = null;
                this.y = null;
                this.z = null;
                this.pitch = null;
                this.yaw = null;
                this.roll = null;
                this.bendAxis = null;
                this.bend = null;
            }
        }


        public Pair<Float, Float> getBend(Pair<Float, Float> value0) {
            if(bend == null) return value0;
            return new Pair<>(this.bendAxis.getValueAtCurrentTick(value0.getLeft()), this.bend.getValueAtCurrentTick(value0.getRight()));
        }

        public Vec3f getBodyOffset(Vec3f value0) {
            if(this.part == null) return value0;
            float x = this.x.getValueAtCurrentTick(value0.getX());
            float y = this.y.getValueAtCurrentTick(value0.getY());
            float z = this.z.getValueAtCurrentTick(value0.getZ());
            return new Vec3f(x, y, z);
        }

        public Vec3f getBodyRotation(Vec3f value0) {
            if(this.part == null) return value0;
            return new Vec3f(
                    this.pitch.getValueAtCurrentTick(value0.getX()),
                    this.yaw.getValueAtCurrentTick(value0.getY()),
                    this.roll.getValueAtCurrentTick(value0.getZ())
            );
        }

    }

    public class Axis {
        protected final EmoteData.StateCollection.State keyframes;


        public Axis(EmoteData.StateCollection.State keyframes) {
            this.keyframes = keyframes;
        }

        private EmoteData.KeyFrame findBefore(int pos, float currentState) {
            if (pos == -1) {
                return (currentTick < data.beginTick) ?
                        new EmoteData.KeyFrame(0, currentState) :
                        (currentTick < data.endTick) ?
                                new EmoteData.KeyFrame(data.beginTick, keyframes.defaultValue) :
                                new EmoteData.KeyFrame(data.endTick, keyframes.defaultValue);
            }
            return this.keyframes.keyFrames.get(pos);
        }

        private EmoteData.KeyFrame findAfter(int pos, float currentState) {
            if (this.keyframes.length() > pos + 1) {
                return this.keyframes.keyFrames.get(pos + 1);
            }

            return currentTick >= data.endTick ?
                    new EmoteData.KeyFrame(data.stopTick, currentState) :
                    currentTick >= getData().beginTick ?
                            new EmoteData.KeyFrame(getData().endTick, keyframes.defaultValue) :
                            new EmoteData.KeyFrame(getData().beginTick, keyframes.defaultValue);
        }


        /**
         * Get the current value of this axis.
         *
         * @param currentValue the Current value of the axis
         * @return value
         */
        public float getValueAtCurrentTick(float currentValue) {
            if(keyframes.isEnabled) {
                int pos = keyframes.findAtTick(currentTick);
                EmoteData.KeyFrame keyBefore = findBefore(pos, currentValue);
                if (isLoopStarted && keyBefore.tick < data.returnToTick) {
                    keyBefore = findBefore(keyframes.findAtTick(data.endTick), currentValue);
                }
                EmoteData.KeyFrame keyAfter = findAfter(pos, currentValue);
                if (data.isInfinite && keyAfter.tick >= data.endTick) {
                    keyAfter = findAfter(keyframes.findAtTick(data.returnToTick - 1), currentValue);
                }
                return getValueFromKeyframes(keyBefore, keyAfter);
            }
            return currentValue;
        }

        /**
         * Calculate the current value between keyframes
         *
         * @param before Keyframe before
         * @param after  Keyframe after
         * @return value
         */
        protected final float getValueFromKeyframes(EmoteData.KeyFrame before, EmoteData.KeyFrame after) {
            int tickBefore = before.tick;
            int tickAfter = after.tick;
            if (tickBefore >= tickAfter) {
                if (currentTick < tickBefore) tickBefore -= data.endTick - data.returnToTick;
                else tickAfter += data.endTick - data.returnToTick;
            }
            if (tickBefore == tickAfter) return before.value;
            float f = (currentTick + tickDelta - (float) tickBefore) / (tickAfter - tickBefore);
            return MathHelper.lerp(Easing.easingFromEnum(data.isEasingBefore ? after.ease : before.ease, f), before.value, after.value);
        }

    }

    public class RotationAxis extends Axis {

        public RotationAxis(EmoteData.StateCollection.State keyframes) {
            super(keyframes);
        }

        @Override
        public float getValueAtCurrentTick(float currentValue) {
            return MathHelper.clampToRadian(super.getValueAtCurrentTick(MathHelper.clampToRadian(currentValue)));
        }
    }
}
