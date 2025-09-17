package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerAnimationController extends AnimationController {
    //Bone pivot point positions used to apply custom pivot point translations.
    private static final Map<String, Vec3f> BONE_POSITIONS = Map.of(
            "right_arm", new Vec3f(5, 22, 0),
            "left_arm", new Vec3f(-5, 22, 0),
            "left_leg", new Vec3f(-2f, 12, 0f),
            "right_leg", new Vec3f(2f, 12, 0f),
            "torso", new Vec3f(0, 24, 0),
            "head", new Vec3f(0, 24, 0),
            "body", new Vec3f(0, 12, 0),
            "cape", new Vec3f(0, 24, 2),
            "elytra", new Vec3f(0, 24, 2)
    );

    //Used for applying torso bend to bones like the head.
    protected List<AdvancedPlayerAnimBone> top_bones;

    protected final PlayerEntity player;

    /**
     * Instantiates a new {@code AnimationController}
     *
     * @param player           The object that will be animated by this controller
     * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
     */
    public PlayerAnimationController(PlayerEntity player, AnimationStateHandler animationHandler) {
        super(animationHandler);
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public boolean triggerAnimation(String newAnimation, float startAnimFrom) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            triggerAnimation(PlayerAnimResources.getAnimation(newAnimation), startAnimFrom);
            return true;
        }
        return false;
    }

    public boolean triggerAnimation(String newAnimation) {
        return triggerAnimation(newAnimation, 0);
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable String newAnimation, boolean fadeFromNothing) {
        if (PlayerAnimResources.hasAnimation(newAnimation)) {
            replaceAnimationWithFade(fadeModifier, PlayerAnimResources.getAnimation(newAnimation), fadeFromNothing);
            return true;
        }
        return false;
    }

    public boolean replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable String newAnimation) {
        return replaceAnimationWithFade(fadeModifier, newAnimation, true);
    }

    @Override
    public void registerBones() {
        this.top_bones = new ArrayList<>();

        this.registerPlayerAnimBone("body");
        this.registerTopPlayerAnimBone("right_arm");
        this.registerTopPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerTopPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerTopPlayerAnimBone("cape");
        this.registerPlayerAnimBone("elytra");
    }

    public void registerTopPlayerAnimBone(String name) {
        this.top_bones.add(this.registerPlayerAnimBone(name));
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        super.get3DTransform(bone);

        //Not a part of MC PAL
        //Required for Geyser PAL
        if (activeBones.containsKey(bone.getName()) && bone instanceof AdvancedPlayerAnimBone advancedBone) {
            PlayerAnimBone controllerBone = activeBones.get(bone.getName());
            if (controllerBone instanceof AdvancedPlayerAnimBone advancedControllerBone) {
                advancedBone.scaleXEnabled = advancedControllerBone.scaleXEnabled;
                advancedBone.scaleYEnabled = advancedControllerBone.scaleYEnabled;
                advancedBone.scaleZEnabled = advancedControllerBone.scaleZEnabled;

                advancedBone.positionXEnabled = advancedControllerBone.positionXEnabled;
                advancedBone.positionYEnabled = advancedControllerBone.positionYEnabled;
                advancedBone.positionZEnabled = advancedControllerBone.positionZEnabled;

                advancedBone.rotXEnabled = advancedControllerBone.rotXEnabled;
                advancedBone.rotYEnabled = advancedControllerBone.rotYEnabled;
                advancedBone.rotZEnabled = advancedControllerBone.rotZEnabled;

                advancedBone.bendEnabled = advancedControllerBone.bendEnabled;
            } else advancedBone.setEnabled(true);
        }
        
        return bone;
    }

    @Override
    protected void applyCustomPivotPoints() {
        float bend = bones.get("torso").getBend();
        float absBend = Math.abs(bend);
        if (absBend > 0.001 && (this.currentAnimation != null && this.currentAnimation.animation().data().getNullable(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY) == Boolean.TRUE)) {
            float h = (float) -(1 - Math.cos(absBend));
            float i = (float) (1 - Math.sin(absBend));
            int sign = (int) Math.signum(bend);
            for (AdvancedPlayerAnimBone bone : top_bones) {
                float offset = getBonePosition(bone.getName()).y() - 18;
                this.activeBones.put(bone.getName(), bone);
                bone.rotX += bend;
                bone.positionZ += (offset * i - offset) * sign;
                bone.positionY += offset * h;
                bone.rotXEnabled = true;
                bone.positionYEnabled = true;
                bone.positionZEnabled = true;
            }
        }
        super.applyCustomPivotPoints();
    }

    @Override
    public Vec3f getBonePosition(String name) {
        if (BONE_POSITIONS.containsKey(name)) return BONE_POSITIONS.get(name);
        if (pivotBones.containsKey(name)) return pivotBones.get(name).getPivot();
        return Vec3f.ZERO;
    }
}
