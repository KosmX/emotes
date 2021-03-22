package com.kosmx.emotes.common.emote;

import com.kosmx.emotes.common.tools.Ease;
import com.kosmx.emotes.common.opennbs.NBS;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used to store Emote data
 * Not playable, but decodeable at bukkit server
 */
public class EmoteData {
    //Time, while the player can move to the beginning pose

    public static final StateCollection.State EMPTY_STATE = new StateCollection.State("empty", 0, 0, false);

    public final int beginTick;
    public final int endTick;
    public final int stopTick;
    public final boolean isInfinite;
    //if infinite, where to return
    public final int returnToTick;
    public final StateCollection head;
    public final StateCollection torso;
    public final StateCollection rightArm;
    public final StateCollection leftArm;
    public final StateCollection rightLeg;
    public final StateCollection leftLeg;
    public final boolean isEasingBefore;
    public final boolean nsfw;

    @Nullable
    public NBS song;

    public static float staticThreshold = 8;


    private EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick, StateCollection head, StateCollection torso, StateCollection rightArm, StateCollection leftArm, StateCollection rightLeg, StateCollection leftLeg, boolean isEasingBefore, boolean nsfw){
        this.beginTick = Math.max(beginTick, 0);
        this.endTick = Math.max(beginTick + 1, endTick);
        this.stopTick = stopTick <= endTick ? endTick + 3 : stopTick;
        this.isInfinite = isInfinite;
        this.returnToTick = returnToTick;
        this.head = head;
        this.torso = torso;
        this.rightArm = rightArm;
        this.rightLeg = rightLeg;
        this.leftArm = leftArm;
        this.leftLeg = leftLeg;
        this.isEasingBefore = isEasingBefore;
        this.nsfw = nsfw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmoteData)) return false;

        EmoteData emoteData = (EmoteData) o;

        if (beginTick != emoteData.beginTick) return false;
        if (endTick != emoteData.endTick) return false;
        if (stopTick != emoteData.stopTick) return false;
        if (isInfinite != emoteData.isInfinite) return false;
        if (returnToTick != emoteData.returnToTick) return false;
        if (isEasingBefore != emoteData.isEasingBefore) return false;
        if (!head.equals(emoteData.head)) return false;
        if (!torso.equals(emoteData.torso)) return false;
        if (!rightArm.equals(emoteData.rightArm)) return false;
        if (!leftArm.equals(emoteData.leftArm)) return false;
        if (!rightLeg.equals(emoteData.rightLeg)) return false;
        return leftLeg.equals(emoteData.leftLeg);
    }

    @Override
    public int hashCode() {
        int result = beginTick;
        result = 31 * result + endTick;
        result = 31 * result + stopTick;
        result = 31 * result + (isInfinite ? 1 : 0);
        result = 31 * result + returnToTick;
        result = 31 * result + head.hashCode();
        result = 31 * result + torso.hashCode();
        result = 31 * result + rightArm.hashCode();
        result = 31 * result + leftArm.hashCode();
        result = 31 * result + rightLeg.hashCode();
        result = 31 * result + leftLeg.hashCode();
        result = 31 * result + (isEasingBefore ? 1 : 0);
        return result;
    }

    public boolean isPlayingAt(int tick){
        return isInfinite || tick < stopTick && tick > 0;
    }

    public static class StateCollection {
        public final String name;
        public final State x;
        public final State y;
        public final State z;
        public final State pitch;
        public final State yaw;
        public final State roll;
        public final State bend;
        public final State bendDirection;
        public final boolean isBendable;

        public StateCollection(float x, float y, float z, float pitch, float yaw, float roll, String name, float translationThreshold, boolean bendable) {
            this.name = name;
            this.x = new State("x", x, translationThreshold, false);
            this.y = new State("y", y, translationThreshold, false);
            this.z = new State("z", z, translationThreshold, false);
            this.pitch = new State("pitch", pitch, 0, true);
            this.yaw = new State("yaw", yaw, 0, true);
            this.roll = new State("roll", roll, 0, true);
            this.bendDirection = new State("axis", 0, 0, true);
            this.bend = new State("bend", 0, 0, true);
            this.isBendable = bendable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateCollection)) return false;

            StateCollection that = (StateCollection) o;

            if (isBendable != that.isBendable) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (!x.equals(that.x)) return false;
            if (!y.equals(that.y)) return false;
            if (!z.equals(that.z)) return false;
            if (!pitch.equals(that.pitch)) return false;
            if (!yaw.equals(that.yaw)) return false;
            if (!roll.equals(that.roll)) return false;
            if (bend != null ? !bend.equals(that.bend) : that.bend != null) return false;
            return bendDirection != null ? bendDirection.equals(that.bendDirection) : that.bendDirection == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + x.hashCode();
            result = 31 * result + y.hashCode();
            result = 31 * result + z.hashCode();
            result = 31 * result + pitch.hashCode();
            result = 31 * result + yaw.hashCode();
            result = 31 * result + roll.hashCode();
            result = 31 * result + (bend != null ? bend.hashCode() : 0);
            result = 31 * result + (bendDirection != null ? bendDirection.hashCode() : 0);
            result = 31 * result + (isBendable ? 1 : 0);
            return result;
        }


        public static class State{
            public final float defaultValue;
            public final float threshold;
            public final List<KeyFrame> keyFrames = new ArrayList<>();
            public final String name;
            private final boolean isAngle;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof State)) return false;

                State state = (State) o;

                if (Float.compare(state.defaultValue, defaultValue) != 0) return false;
                if (Float.compare(state.threshold, threshold) != 0) return false;
                if (isAngle != state.isAngle) return false;
                if (!keyFrames.equals(state.keyFrames)) return false;
                return Objects.equals(name, state.name);
            }

            @Override
            public int hashCode() {
                int result = (defaultValue != +0.0f ? Float.floatToIntBits(defaultValue) : 0);
                result = 31 * result + (threshold != +0.0f ? Float.floatToIntBits(threshold) : 0);
                result = 31 * result + keyFrames.hashCode();
                result = 31 * result + (name != null ? name.hashCode() : 0);
                result = 31 * result + (isAngle ? 1 : 0);
                return result;
            }

            /**
             * @param name Name (for import stuff)
             * @param defaultValue default value
             * @param threshold threshold for validation
             * @param isAngle isAngle value (if false then it's a translation)
             */
            private State(String name, float defaultValue, float threshold, boolean isAngle) {
                this.defaultValue = defaultValue;
                this.threshold = threshold;
                this.name = name;
                this.isAngle = isAngle;
            }

            public int length(){
                return keyFrames.size();
            }

            /**
             * Find the last keyframe's number before the tick
             * @param tick tick
             * @return given keyframe
             */
            public int findAtTick(int tick){
                int i = - 1;
                while(this.keyFrames.size() > i + 1 && this.keyFrames.get(i + 1).tick <= tick){
                    i++;
                }
                return i;
            }

            /**
             * Add a new keyframe to the emote
             * @param tick where
             * @param value what value
             * @param ease with what easing
             * @param rotate 360 degrees turn
             * @param degrees is the value in degrees (or radians if false
             * @return is the keyframe valid
             */
            public boolean addKeyFrame(int tick, float value, Ease ease, int rotate, boolean degrees){
                if(degrees && this.isAngle) value *= 0.01745329251f;
                boolean bl = this.addKeyFrame(new KeyFrame(tick, value, ease));
                if(isAngle && rotate != 0){
                    bl = this.addKeyFrame(new KeyFrame(tick, (float) (value + Math.PI * 2d * rotate), ease)) && bl;
                }
                return bl;
            }

            /**
             * Add a new keyframe to the emote
             * @param tick where
             * @param value what value
             * @param ease with what easing
             * @return is the keyframe valid
             */
            public boolean addKeyFrame(int tick, float value, Ease ease){
                if(value == Float.NaN)throw new IllegalArgumentException("value can't be NaN");
                return this.addKeyFrame(new KeyFrame(tick, value, ease));
            }

            /**
             * Internal add keyframe method
             * @param keyFrame what
             * @return is valid keyframe
             */
            private boolean addKeyFrame(KeyFrame keyFrame){
                int i = findAtTick(keyFrame.tick) + 1;
                this.keyFrames.add(i, keyFrame);
                return this.isAngle || !(Math.abs(this.defaultValue - keyFrame.value) > this.threshold);
            }

            public void replace(KeyFrame keyFrame, int pos){
                this.keyFrames.remove(pos);
                this.keyFrames.add(pos, keyFrame);
            }

            public void replaceEase(int pos, Ease ease){
                KeyFrame original = this.keyFrames.get(pos);
                replace(new KeyFrame(original.tick, original.value, ease), pos);
            }
        }
    }
    public static class KeyFrame{

        public final int tick;
        public final Float value;
        public final Ease ease;

        public KeyFrame(int tick, float value, Ease ease){
            this.tick = tick;
            this.value = value;
            this.ease = ease;
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof KeyFrame){
                return ((KeyFrame) other).ease == this.ease && ((KeyFrame) other).tick == this.tick && ((KeyFrame) other).value.equals(this.value);
            }
            else return super.equals(other);
        }

        public KeyFrame(int tick, float value){
            this(tick, value, Ease.INOUTSINE);
        }

        @Override
        public int hashCode() {
            int result = tick;
            result = 31 * result + value.hashCode();
            result = 31 * result + ease.hashCode();
            return result;
        }
    }

    public static class EmoteBuilder{

        public final StateCollection head;
        public final StateCollection torso;
        public final StateCollection rightArm;
        public final StateCollection leftArm;
        public final StateCollection rightLeg;
        public final StateCollection leftLeg;
        public boolean isEasingBefore = false;
        public float validationThreshold = staticThreshold;
        public boolean nsfw = false;

        public int beginTick = 0;
        public int endTick;
        public int stopTick = 0;
        public boolean isLooped = false;
        public int returnTick;


        public EmoteBuilder(){
            head = new StateCollection(0, 0, 0, 0, 0, 0, "head", validationThreshold, false);
            torso = new StateCollection(0, 0, 0, 0, 0, 0, "torso", validationThreshold / 8f, true);
            rightArm = new StateCollection(- 5, 2, 0, 0, 0,0f, "rightArm", validationThreshold, true);
            leftArm = new StateCollection(5, 2, 0, 0, 0,0f, "leftArm", validationThreshold, true);
            leftLeg = new StateCollection(1.9f, 12, 0.1f, 0, 0, 0, "leftLeg", validationThreshold, true);
            rightLeg = new StateCollection(- 1.9f, 12, 0.1f, 0, 0, 0, "rightLeg", validationThreshold, true);
        }

        public EmoteData build(){
            return new EmoteData(beginTick, endTick, stopTick, isLooped, returnTick, head, torso, rightArm, leftArm, rightLeg, leftLeg, isEasingBefore, nsfw);
        }
    }
}
