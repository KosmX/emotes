package io.github.kosmx.emotes.common.emote;

import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.common.opennbs.NBS;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

//TODO
/**
 * Used to store Emote data
 */
public final class EmoteData implements Supplier<UUID> {
    //Time, while the player can move to the beginning pose

    public static final StateCollection.State EMPTY_STATE = new StateCollection.State("empty", 0, 0, false);

    public final int beginTick;
    public final int endTick;
    public final int stopTick;
    public final boolean isInfinite;
    //if infinite, where to return
    public final int returnToTick;

    public final HashMap<String, StateCollection> bodyParts;
    //Deprecated variables will be removed in the animation rework part.
    @Deprecated
    public final StateCollection head;
    @Deprecated
    public final StateCollection body;
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

    //Emote identifier code.
    private final UUID uuid;

    //Store emote data in the emote object
    @Nullable
    public final String name;
    @Nullable
    public final String description;
    @Nullable
    public final String author;

    @Nullable
    public NBS song;

    public static float staticThreshold = 8;
    public final EmoteFormat emoteFormat;

    @Nullable
    public ByteBuffer iconData;

    public boolean isBuiltin = false;


    private EmoteData(int beginTick, int endTick, int stopTick, boolean isInfinite, int returnToTick, HashMap<String, StateCollection> bodyParts, boolean isEasingBefore, boolean nsfw, UUID uuid, @Nullable String name, @Nullable String description, @Nullable String author, EmoteFormat emoteFormat, ByteBuffer iconData, NBS song){
        this.beginTick = Math.max(beginTick, 0);
        this.name = name;
        this.description = description;
        this.author = author;
        this.endTick = Math.max(beginTick + 1, endTick);
        this.stopTick = stopTick <= endTick ? endTick + 3 : stopTick;
        this.isInfinite = isInfinite;
        this.returnToTick = returnToTick;
        this.bodyParts = bodyParts;
        this.isEasingBefore = isEasingBefore;
        this.nsfw = nsfw;
        if(uuid == null){
            uuid = this.generateUuid();
        }
        this.uuid = uuid;
        this.emoteFormat = emoteFormat;
        this.iconData = iconData;
        this.song = song;
        head = bodyParts.get("head");
        body = bodyParts.get("body");
        rightArm = bodyParts.get("rightArm");
        leftArm = bodyParts.get("leftArm");
        rightLeg = bodyParts.get("rightLeg");
        leftLeg = bodyParts.get("leftLeg");
        assert emoteFormat != null;
    }

    /**
     * Some data from source are ignored
     * @param o
     * @return
     */
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
        if (!Objects.equals(this.iconData, emoteData.iconData)) return false;

        return bodyParts.equals(emoteData.bodyParts);
    }

    @Override
    public int hashCode() {
        int result = beginTick;
        result = 31 * result + endTick;
        result = 31 * result + stopTick;
        result = 31 * result + (isInfinite ? 1 : 0);
        result = 31 * result + returnToTick;
        result = 31 * result + (isEasingBefore ? 1 : 0);
        result = 31 * result + bodyParts.hashCode();
        return result;
    }

    private UUID generateUuid(){
        int result = beginTick;
        result = 31 * result + endTick;
        result = 31 * result + stopTick;
        result = 31 * result + (isInfinite ? 1 : 0);
        result = 31 * result + returnToTick;
        result = 31 * result + (isEasingBefore ? 1 : 0);

        long dataHash = result * 31 + this.bodyParts.hashCode();

        long nameHash = this.name == null ? 0 : name.hashCode();
        long descHash = this.description == null ? 0 : description.hashCode();
        long authHash = result * 31 + (this.author == null ? 0 : author.hashCode());
        //long iconHash = this.iconData == null ? 0 : iconData.hashCode() + authHash * 31;


        return new UUID(dataHash << Integer.SIZE + nameHash, descHash << Integer.SIZE + authHash);
    }

    public boolean isPlayingAt(int tick){
        return isInfinite || tick < stopTick && tick > 0;
    }

    /**
     * Uuid of the emote. used for key binding and for server-client identification
     * @return UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public UUID get() {
        return this.uuid;
    }

    /**
     * Will return invalid information if {@link EmoteData#isInfinite} is true
     * @return The length of the emote in ticks (20 t/s)
     */
    public int getLength() {
        return stopTick;
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
        public final StateCollection body;
        public final StateCollection rightArm;
        public final StateCollection leftArm;
        public final StateCollection rightLeg;
        public final StateCollection leftLeg;
        public boolean isEasingBefore = false;
        //public float validationThreshold = staticThreshold;
        public boolean nsfw = false;
        private final HashMap<String, StateCollection> bodyParts = new HashMap<>();

        /**
         * If you want auto-uuid, leave it null
         */
        @Nullable
        public UUID uuid = null;

        public int beginTick = 0;
        public int endTick;
        public int stopTick = 0;
        public boolean isLooped = false;
        public int returnTick;
        final EmoteFormat emoteEmoteFormat;

        private final float validationThreshold;

        public String name = null;
        @Nullable
        public String description = null;
        @Nullable
        public String author = null;

        @Nullable
        public NBS song = null;

        @Nullable
        public ByteBuffer iconData;

        public EmoteBuilder(EmoteFormat source){
            this(staticThreshold, source);
        }

        public EmoteBuilder(float validationThreshold, EmoteFormat emoteFormat){
            this.validationThreshold = validationThreshold;
            head = new StateCollection(0, 0, 0, 0, 0, 0, "head", validationThreshold, false);
            body = new StateCollection(0, 0, 0, 0, 0, 0, "body",validationThreshold / 8f, true);
            rightArm = new StateCollection(- 5, 2, 0, 0, 0,0f, "rightArm", validationThreshold, true);
            leftArm = new StateCollection(5, 2, 0, 0, 0,0f, "leftArm", validationThreshold, true);
            leftLeg = new StateCollection(1.9f, 12, 0.1f, 0, 0, 0, "leftLeg", validationThreshold, true);
            rightLeg = new StateCollection(- 1.9f, 12, 0.1f, 0, 0, 0, "rightLeg", validationThreshold, true);

            bodyParts.put("head", head);
            bodyParts.put("body", body);
            bodyParts.put("rightArm", rightArm);
            bodyParts.put("rightLeg", rightLeg);
            bodyParts.put("leftArm", leftArm);
            bodyParts.put("leftLeg", leftLeg);
            this.emoteEmoteFormat = emoteFormat;
        }

        public EmoteBuilder setDescription(String s){
            description = s;
            return this;
        }
        public EmoteBuilder setName(String s){
            name = s;
            return this;
        }
        public EmoteBuilder setAuthor(String s){
            author = s;
            return this;
        }
        /**
         * Create a new part. X, Y, Z the default offsets, pitch, yaw, roll are the default rotations.
         *
         *
         * @param name name
         * @param x x
         * @param y y
         * @param z z
         * @param pitch pitch
         * @param yaw yaw
         * @param roll roll
         * @param bendable is it bendable
         * @return ...
         */
        public StateCollection getOrCreateNewPart(String name, float x, float y, float z, float pitch, float yaw, float roll, boolean bendable){
            if(!bodyParts.containsKey(name)){
                bodyParts.put(name, new StateCollection(x, y, z, pitch, yaw, roll, name, validationThreshold, bendable));
            }
            return bodyParts.get(name);
        }

        /**
         * Get a part with a name.
         * @param name name
         * @return ...
         */
        @Nullable
        public StateCollection getPart(String name){
            return bodyParts.get(name);
        }


        public EmoteBuilder fullyEnableParts(){
            for(Map.Entry<String, StateCollection> part:bodyParts.entrySet()){
                part.getValue().fullyEnablePart(false);
            }
            return this;
        }

        /**
         * Remove unnecessary keyframes from this emote.
         * If the keyframe before and after are the same as the currently checked, the keyframe will be removed
         *
         */
        public EmoteBuilder optimizeEmote(){
            for(Map.Entry<String, StateCollection> part:bodyParts.entrySet()){
                part.getValue().optimize(isLooped, returnTick);
            }
            return this;
        }

        public EmoteData build(){
            return new EmoteData(beginTick, endTick, stopTick, isLooped, returnTick, bodyParts, isEasingBefore, nsfw, uuid, name, description, author, emoteEmoteFormat, iconData, song);
        }

        public EmoteBuilder setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
    }
}
