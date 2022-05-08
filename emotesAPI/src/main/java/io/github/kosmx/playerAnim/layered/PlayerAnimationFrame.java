package io.github.kosmx.playerAnim.layered;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin it into a player, add to its Animation stack,
 * and override its tick,
 *
 * It is a representation of your pose on the frame.
 * Override {@link IAnimation#setupAnim(float)} and set the pose there.
 */
public abstract class PlayerAnimationFrame implements IAnimation {

    protected PlayerPart head = new PlayerPart();
    protected PlayerPart body = new PlayerPart();
    protected PlayerPart rightArm = new PlayerPart();
    protected PlayerPart leftArm = new PlayerPart();
    protected PlayerPart rightLeg = new PlayerPart();
    protected PlayerPart leftLeg = new PlayerPart();
    protected PlayerPart rightItem = new PlayerPart();
    protected PlayerPart leftItem = new PlayerPart();

    HashMap<String, PlayerPart> parts = new HashMap<>();

    public PlayerAnimationFrame() {
        parts.put("head", head);
        parts.put("body", body);
        parts.put("rightArm", rightArm);
        parts.put("leftArm", leftArm);
        parts.put("rightLeg", rightLeg);
        parts.put("leftLeg", leftLeg);
        parts.put("rightItem", rightItem);
        parts.put("leftItem", leftItem);
    }


    @Override
    public void tick() {
        IAnimation.super.tick();
    }

    @Override
    public boolean isActive() {
        for (Map.Entry<String, PlayerPart> entry: parts.entrySet()) {
            PlayerPart part = entry.getValue();
            if (part.bend != null || part.pos != null || part.rot != null) return true;
        }
        return false;
    }

    /**
     * Reset every part, those parts won't influence the animation
     * Don't use it if you don't want to set every part in every frame
     */
    public void resetPose() {
        for (Map.Entry<String, PlayerPart> entry: parts.entrySet()) {
            entry.getValue().setNull();
        }
    }


    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        PlayerPart part = parts.get(modelName);
        if (part == null) return value0;
        switch (type) {
            case POSITION:
                return part.pos == null ? value0 : part.pos;
            case ROTATION:
                return part.rot == null ? value0 : part.rot;
            case BEND:
                return part.bend == null ? value0 : new Vec3f(part.bend.getLeft(), part.bend.getRight(), 0f);
            default:
                return value0;
        }
    }

    public static class PlayerPart {
        public Vec3f pos;
        public Vec3f rot;
        public Pair<Float, Float> bend;

        protected void setNull() {
            pos = rot = null;
            bend = null;
        }
    }
}
