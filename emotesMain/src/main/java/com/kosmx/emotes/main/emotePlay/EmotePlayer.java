package com.kosmx.emotes.main.emotePlay;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.opennbs.SoundPlayer;
import com.kosmx.emotes.common.opennbs.format.Layer;
import com.kosmx.emotes.common.tools.Easing;
import com.kosmx.emotes.common.tools.MathHelper;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.common.tools.Vector3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

// abstract to extend it in every environments
public abstract class EmotePlayer {
    private final EmoteData data;
    @Nullable
    final SoundPlayer song;
    private boolean isRunning = true;
    private int currentTick = 0;
    private boolean isLoopStarted = false;

    protected float tickDelta;

    public final BodyPart head;
    public final BodyPart torso;
    public final BodyPart rightArm;
    public final BodyPart leftArm;
    public final BodyPart rightLeg;
    public final BodyPart leftLeg;

    @Nullable

    /**
     *
     * @param emote EmoteData to play
     */
    public EmotePlayer(EmoteData emote, Consumer<Layer.Note> noteConsumer) {
        this.data = emote;
        if (emote.song != null) {
            this.song = new SoundPlayer(emote.song, noteConsumer, 0);
        }
        else {
            this.song = null;
        }

        head = partConstructor(data.head);
        torso = partConstructor(data.torso);
        rightArm = partConstructor(data.rightArm);
        leftArm = partConstructor(data.leftArm);
        rightLeg = partConstructor(data.rightLeg);
        leftLeg = partConstructor(data.leftLeg);
    }

    protected abstract <T extends BodyPart> T partConstructor(EmoteData.StateCollection part);

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
            if (SoundPlayer.isPlayingSong(this.song)) song.tick();
        }
    }

    /**
     * Is emotePlayer running
     *
     * @param emote EmotePlayer, can be null
     * @return is running
     */
    public static boolean isRunningEmote(@Nullable EmotePlayer emote) {
        return emote != null && emote.isRunning;
    }

    public void stop() {
        isRunning = false;
        //if(this.perspectiveRedux) PerspectiveReduxProxy.setPerspective(false);
        //if(this.perspective != null) MinecraftClient.getInstance().options.setPerspective(perspective); //TODO
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

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
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

    public abstract class BodyPart {
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

        /**
         * public void updateBodyPart(ModelPart modelPart){
         * modelPart.pivotX = x.getValueAtCurrentTick(modelPart.pivotX);
         * modelPart.pivotY = y.getValueAtCurrentTick(modelPart.pivotY);
         * modelPart.pivotZ = z.getValueAtCurrentTick(modelPart.pivotZ);
         * modelPart.pitch = pitch.getValueAtCurrentTick(modelPart.pitch);
         * modelPart.yaw = yaw.getValueAtCurrentTick(modelPart.yaw);
         * modelPart.roll = roll.getValueAtCurrentTick(modelPart.roll);
         * }
         */
        public abstract <T> void updateBodyPart(T modelPart);

        public Pair<Float, Float> getBend() {
            return new Pair<>(this.bendAxis.getValueAtCurrentTick(0), this.bend.getValueAtCurrentTick(0));
        }

        public Vector3<Double> getBodyOffset() {
            double x = this.x.getValueAtCurrentTick(0);
            double y = this.y.getValueAtCurrentTick(0);
            double z = this.z.getValueAtCurrentTick(0);
            return new Vector3<>(x, y, z);
        }

        public Vector3<Float> getBodyRotation() {
            return new Vector3<>(
                    this.pitch.getValueAtCurrentTick(0),
                    this.yaw.getValueAtCurrentTick(0),
                    this.roll.getValueAtCurrentTick(0)
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
                return (currentTick < data.beginTick || keyframes.length() != 0) ?
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
            return (currentTick >= data.endTick || this.keyframes.length() != 0) && !data.isInfinite ?
                    new EmoteData.KeyFrame(data.stopTick, currentState) :
                    (currentTick >= getData().beginTick) ?
                            new EmoteData.KeyFrame(getData().endTick, keyframes.defaultValue) :
                            new EmoteData.KeyFrame(getData().beginTick, keyframes.defaultValue);
        }

        /**
         * Get the current value of this axis.
         *
         * @param currentValue the Current value of the axis
         * @return value
         */
        protected float getValueAtCurrentTick(float currentValue) {
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
        protected float getValueAtCurrentTick(float currentValue) {
            return MathHelper.clampToRadian(super.getValueAtCurrentTick(MathHelper.clampToRadian(currentValue)));
        }
    }
}