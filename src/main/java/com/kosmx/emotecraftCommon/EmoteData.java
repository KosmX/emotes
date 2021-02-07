package com.kosmx.emotecraftCommon;

import com.kosmx.emotecraftCommon.math.Ease;

import java.util.ArrayList;
import java.util.List;

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

    public static float threshold = 8; //TODO default threshold

    /**
     * Create new EmoteData
     * @param beginTick begin
     * @param endTick end
     * @param stopTick last tick
     * @param isInfinite is looped
     * @param returnToTick to return
     * @param threshold threshold value
     */
    public EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick, float threshold) {
        this.beginTick = Math.max(beginTick, 0);
        this.endTick = Math.max(beginTick + 1, endTick);
        this.stopTick = stopTick <= endTick ? endTick + 3 : stopTick;
        this.isInfinite = isInfinite;
        this.returnToTick = returnToTick;
        head = new StateCollection(0, 0, 0, 0, 0, 0, "head", threshold);
        torso = new StateCollection(0, 0, 0, 0, 0, 0, "torso", threshold / 8f);
        rightArm = new StateCollection(- 5, 2, 0, 0, 0, 0.09f, "rightArm", threshold);
        leftArm = new StateCollection(5, 2, 0, 0, 0, - 0.09f, "leftArm", threshold);
        leftLeg = new StateCollection(1.9f, 12, 0.1f, 0, 0, 0, "leftLeg", threshold);
        rightLeg = new StateCollection(- 1.9f, 12, 0.1f, 0, 0, 0, "rightLeg", threshold);
    }

    private EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick, StateCollection head, StateCollection torso, StateCollection rightArm, StateCollection leftArm, StateCollection rightLeg, StateCollection leftLeg){
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
    }

    /**
     * Create new EmoteData
     * @param beginTick begin
     * @param endTick end
     * @param stopTick stop
     * @param isInfinite looped
     * @param returnToTick to return
     */
    public EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick){
        this(beginTick, endTick, stopTick, isInfinite, returnToTick, threshold);
    }

    @Deprecated
    public EmoteData(int beginTick, int endTick, int stopTick){
        this(beginTick, endTick, stopTick, false, 0, threshold);
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

        public StateCollection(float x, float y, float z, float pitch, float yaw, float roll, String name, float translationThreshold){
            this(x, y, z, pitch, yaw, roll, name, translationThreshold, true);
        }


        public static class State{
            public final float defaultValue;
            public final float threshold;
            public final List<KeyFrame> keyFrames = new ArrayList<>();
            public final String name;
            private final boolean isAngle;

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
                    bl = this.addKeyFrame(new KeyFrame(tick, (float) (value + Math.PI * 2d) * rotate, ease)) && bl;
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

        public KeyFrame(int tick, float value){
            this(tick, value, Ease.INOUTSINE);
        }

    }

    public static class EmoteBuilder{

        public final StateCollection head;
        public final StateCollection torso;
        public final StateCollection rightArm;
        public final StateCollection leftArm;
        public final StateCollection rightLeg;
        public final StateCollection leftLeg;

        public int beginTick;
        public int endTick;
        public int stopTick;
        public boolean isLooped;
        public int returnTick;


        public EmoteBuilder(){
            head = new StateCollection(0, 0, 0, 0, 0, 0, "head", threshold);
            torso = new StateCollection(0, 0, 0, 0, 0, 0, "torso", threshold / 8f);
            rightArm = new StateCollection(- 5, 2, 0, 0, 0, 0.09f, "rightArm", threshold);
            leftArm = new StateCollection(5, 2, 0, 0, 0, - 0.09f, "leftArm", threshold);
            leftLeg = new StateCollection(1.9f, 12, 0.1f, 0, 0, 0, "leftLeg", threshold);
            rightLeg = new StateCollection(- 1.9f, 12, 0.1f, 0, 0, 0, "rightLeg", threshold);
        }

        public EmoteData build(){
            return new EmoteData(beginTick, endTick, stopTick, isLooped, returnTick, head, torso, rightArm, leftArm, rightLeg, leftLeg);
        }
    }
}
