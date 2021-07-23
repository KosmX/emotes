package io.github.kosmx.emotes.common.emote;

import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.common.opennbs.NBS;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//TODO
/**
 * Used to store Emote data
 */
public final class EmoteData {
    //Time, while the player can move to the beginning pose

    public static final StateCollection.State EMPTY_STATE = new StateCollection.State("empty", 0, 0, false);

    public final int beginTick;
    public final int endTick;
    public final int stopTick;
    public final boolean isInfinite;
    //if infinite, where to return
    public final int returnToTick;

    public final HashMap<String, StateCollection> bodyParts = new HashMap<>();
    //Deprecated variables will be removed in the animation rework part.
    @Deprecated
    public final StateCollection head;
    @Deprecated
    public final StateCollection torso;
    @Deprecated
    public final StateCollection rightArm;
    @Deprecated
    public final StateCollection leftArm;
    @Deprecated
    public final StateCollection rightLeg;
    @Deprecated
    public final StateCollection leftLeg;
    public final boolean isEasingBefore;
    public final boolean nsfw;

    //Store emote data in the emote object
    @Nullable
    public String name = null;
    @Nullable
    public String description = null;
    @Nullable
    public String author = null;

    @Nullable
    public NBS song;

    public static float staticThreshold = 8;
    public final Source source;

    @Nullable
    public ByteBuffer iconData;


    private EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick, StateCollection head, StateCollection torso, StateCollection rightArm, StateCollection leftArm, StateCollection rightLeg, StateCollection leftLeg, boolean isEasingBefore, boolean nsfw, Source source){
        this.beginTick = Math.max(beginTick, 0);
        this.endTick = Math.max(beginTick + 1, endTick);
        this.stopTick = stopTick <= endTick ? endTick + 3 : stopTick;
        this.isInfinite = isInfinite;
        this.returnToTick = returnToTick;
        bodyParts.put("head", this.head = head);
        bodyParts.put("body", this.torso = torso);
        bodyParts.put("rightArm", this.rightArm = rightArm);
        bodyParts.put("rightLeg", this.rightLeg = rightLeg);
        bodyParts.put("leftArm", this.leftArm = leftArm);
        bodyParts.put("leftLeg", this.leftLeg = leftLeg);
        this.isEasingBefore = isEasingBefore;
        this.nsfw = nsfw;
        assert source != null;
        this.source = source;
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

    public EmoteData setDescription(String s){
        description = s;
        return this;
    }
    public EmoteData setName(String s){
        name = s;
        return this;
    }
    public EmoteData setAuthor(String s){
        author = s;
        return this;
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

    public void fullyEnableParts(){
        head.fullyEnablePart(false);
        torso.fullyEnablePart(false);
        rightArm.fullyEnablePart(false);
        leftArm.fullyEnablePart(false);
        rightLeg.fullyEnablePart(false);
        leftLeg.fullyEnablePart(false);
    }

    /**
     * Remove unnecessary keyframes from this emote.
     * If the keyframe before and after are the same as the currently checked, the keyframe will be removed
     *
     * This function WILL change the its hash, use it ONLY when importing
     */
    public EmoteData optimizeEmote(){
        head.optimize(isInfinite, returnToTick);
        torso.optimize(isInfinite, returnToTick);
        leftArm.optimize(isInfinite, returnToTick);
        rightArm.optimize(isInfinite, returnToTick);
        leftLeg.optimize(isInfinite, returnToTick);
        rightLeg.optimize(isInfinite, returnToTick);
        return this;
    }

    public static class StateCollection {
        @Deprecated
        public final String name;
        public final State x;
        public final State y;
        public final State z;
        public final State pitch;
        public final State yaw;
        public final State roll;
        @Nullable
        public final State bend;
        @Nullable
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
            if(bendable) {
                this.bendDirection = new State("axis", 0, 0, true);
                this.bend = new State("bend", 0, 0, true);
            }
            else {
                this.bend = null;
                this.bendDirection = null; //This will causes some errors, but fixes the invalid data problem
            }
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
            int result = 0;
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

        public void fullyEnablePart(boolean always){
            if(always || x.isEnabled || y.isEnabled || z.isEnabled || pitch.isEnabled || yaw.isEnabled || roll.isEnabled || (isBendable && (bend.isEnabled || bendDirection.isEnabled))){
                x.isEnabled = true;
                y.isEnabled = true;
                z.isEnabled = true;
                pitch.isEnabled = true;
                yaw.isEnabled = true;
                roll.isEnabled = true;
                if(isBendable) {
                    bend.isEnabled = true;
                    bendDirection.isEnabled = true;
                }
            }
        }

        protected void optimize(boolean isLooped, int ret){
            x.optimize(isLooped, ret);
            y.optimize(isLooped, ret);
            z.optimize(isLooped, ret);
            pitch.optimize(isLooped, ret);
            yaw.optimize(isLooped, ret);
            roll.optimize(isLooped, ret);
            if(isBendable) {
                bend.optimize(isLooped, ret);
                bendDirection.optimize(isLooped, ret);
            }
        }

        public static class State{
            public final float defaultValue;
            public final float threshold;
            public final List<KeyFrame> keyFrames = new ArrayList<>();
            public final String name;
            private final boolean isAngle;
            public boolean isEnabled = false;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof State)) return false;

                State state = (State) o;

                if (Float.compare(state.defaultValue, defaultValue) != 0) return false;
                if (isAngle != state.isAngle) return false;
                if (!keyFrames.equals(state.keyFrames)) return false;
                if (isEnabled != state.isEnabled)return false;
                return Objects.equals(name, state.name);
            }

            @Override
            public int hashCode() {
                int result = (defaultValue != +0.0f ? Float.floatToIntBits(defaultValue) : 0);
                result = 31 * result + keyFrames.hashCode();
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
                this.isEnabled = true;
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

            protected void optimize(boolean isLooped, int returnToTick){
                for(int i = 1; i < this.keyFrames.size()-1; i++){
                    if(keyFrames.get(i - 1).value != keyFrames.get(i).value){
                        continue;
                    }
                    if(keyFrames.size() <= i + 1 || keyFrames.get(i).value != keyFrames.get(i+1).value){
                        continue;
                    }
                    if(isLooped && keyFrames.get(i-1).tick < returnToTick && keyFrames.get(i).tick >= returnToTick){
                        continue;
                    }
                    keyFrames.remove(i--);
                }
            }
        }
    }
    public static class KeyFrame{

        public final int tick;
        public final float value;
        public final Ease ease;

        public KeyFrame(int tick, float value, Ease ease){
            this.tick = tick;
            this.value = value;
            this.ease = ease;
        }

        @Override
        public boolean equals(Object other) {
            if(other instanceof KeyFrame){
                return ((KeyFrame) other).ease == this.ease && ((KeyFrame) other).tick == this.tick && ((KeyFrame) other).value == this.value;
            }
            else return super.equals(other);
        }

        public KeyFrame(int tick, float value){
            this(tick, value, Ease.INOUTSINE);
        }

        @Override
        public int hashCode() {
            int result = tick;
            result = 31 * result + Float.hashCode(value);
            result = 31 * result + ease.getId();
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
        //public float validationThreshold = staticThreshold;
        public boolean nsfw = false;

        public int beginTick = 0;
        public int endTick;
        public int stopTick = 0;
        public boolean isLooped = false;
        public int returnTick;
        final Source emoteSource;

        public EmoteBuilder(Source source){
            this(staticThreshold, source);
        }

        public EmoteBuilder(float validationThreshold, Source source){
            head = new StateCollection(0, 0, 0, 0, 0, 0, "head", validationThreshold, false);
            torso = new StateCollection(0, 0, 0, 0, 0, 0, "torso",validationThreshold / 8f, true);
            rightArm = new StateCollection(- 5, 2, 0, 0, 0,0f, "rightArm", validationThreshold, true);
            leftArm = new StateCollection(5, 2, 0, 0, 0,0f, "leftArm", validationThreshold, true);
            leftLeg = new StateCollection(1.9f, 12, 0.1f, 0, 0, 0, "leftLeg", validationThreshold, true);
            rightLeg = new StateCollection(- 1.9f, 12, 0.1f, 0, 0, 0, "rightLeg", validationThreshold, true);
            this.emoteSource = source;
        }

        public EmoteData build(){
            return new EmoteData(beginTick, endTick, stopTick, isLooped, returnTick, head, torso, rightArm, leftArm, rightLeg, leftLeg, isEasingBefore, nsfw, emoteSource);
        }
    }
}
