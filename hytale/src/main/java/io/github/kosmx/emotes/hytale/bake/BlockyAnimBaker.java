package io.github.kosmx.emotes.hytale.bake;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.HumanoidAnimationController;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.charset.StandardCharsets;

/**
 * Runs the Emotecraft animation engine over an emote and records the resulting pose into a Hytale {@code .blockyanim}.
 * <p>
 * Hytale's client can only play keyframe clips it already holds — there is no per-bone channel to stream a live pose
 * over, and its animation format carries no expression language to parameterise (unlike Bedrock's Molang, which is what
 * lets the Geyser extension drive bones through entity properties).
 * <p>
 * The pose is sampled rather than transcribed keyframe-for-keyframe on purpose: Emotecraft keeps a separate keyframe
 * list per <i>axis</i>, each with its own easing and molang expressions, whereas a {@code .blockyanim} keyframe carries
 * one whole vector and only knows {@code linear}/{@code smooth}. Merging those axis timelines means evaluating them at
 * every distinct time anyway, so the engine is simply asked for the finished pose.
 */
public final class BlockyAnimBaker extends HumanoidAnimationController {
    /**
     * Identifies the shape of everything the cache holds — what this class and {@link HytaleRig} produce, and what the
     * index records about it. <b>Bump it</b> whenever any of that changes: the retargeting constants are still
     * uncalibrated, and a cached clip carries no hint of the axes it was baked with, so the stamp is the only thing
     * that keeps a recalibration from silently replaying stale poses.
     */
    public static final int VERSION = 1;

    /** Emotecraft animates on Minecraft's 20 ticks/second; Hytale plays clips at 60 fps. */
    private static final int FRAMES_PER_TICK = 3;

    private final JsonObject nodeAnimations = new JsonObject();

    private BlockyAnimBaker() {
        super((a, b, c) -> PlayState.STOP, MolangLoader::createNewEngine);
    }

    /**
     * A finished clip. The frame count is how long it actually turned out to be, which is not always what the emote
     * claims — the engine can stop early — and is the only thing that says when a performance is over.
     */
    public record Baked(byte[] json, int frames) {
    }

    /**
     * @param emote an emote as loaded by {@code UniversalEmoteSerializer}
     * @return a {@code .blockyanim} document ready for {@code CommonAssetRegistry}
     */
    public static Baked bake(Animation emote) {
        BlockyAnimBaker baker = new BlockyAnimBaker();
        baker.triggerAnimation(RawAnimation.begin().thenPlay(emote), 0.0F);

        AnimationData data = new AnimationData(0.0F, 1.0F, false);
        int frames = Math.max(1, Math.round(emote.length() * FRAMES_PER_TICK));
        int baked = frames;

        for (int frame = 0; frame < frames; frame++) {
            int subTick = frame % FRAMES_PER_TICK;
            if (subTick == 0) {
                baker.tick(data);
            }
            if (!baker.isActive()) {
                baked = frame;
                break;
            }

            data.setPartialTick(subTick / (float) FRAMES_PER_TICK);
            baker.setupAnim(data);
            baker.sample(frame);
        }

        JsonObject root = new JsonObject();
        root.addProperty("formatVersion", 1);
        root.addProperty("duration", baked);
        root.addProperty("holdLastKeyframe", false);
        root.add("nodeAnimations", baker.nodeAnimations);
        return new Baked(root.toString().getBytes(StandardCharsets.UTF_8), baked);
    }

    /** How long a clip of this many frames runs, in milliseconds. */
    public static long duration(int frames) {
        return frames * 1000L / (20 * FRAMES_PER_TICK);
    }

    /** Reads every bone the engine touched this frame onto the matching Hytale node. */
    @SuppressWarnings("removal") // PlayerAnimBone#bend is the only way to read Emotecraft's limb flexing
    private void sample(int frame) {
        for (String boneName : this.activeBones.keySet()) {
            String nodeName = HytaleRig.BONES.get(boneName);
            if (nodeName == null) {
                continue; // a bone with no Hytale counterpart, such as the elytra
            }

            // Local transforms, deliberately without parent composition: Hytale's skeleton really does nest
            // Pelvis -> Thigh -> Calf and composes it client-side, unlike the flat Bedrock rig Geyser pre-composes for.
            PlayerAnimBone bone = get3DTransform(boneName);
            if (HytaleRig.CAPE_BONE.equals(boneName)) {
                bone.rotation.x = -bone.rotation.x; // the Geyser controller corrects the same authoring quirk
            }

            Quaternionf orientation = HytaleRig.toHytaleOrientation(bone.rotation);
            Vector3f position = HytaleRig.toHytalePosition(bone.position);
            if (HytaleRig.ROOT_BONE.equals(boneName)) {
                position.add(HytaleRig.pivotCompensation(orientation, HytaleRig.ROOT_PIVOT_OFFSET));
            }

            JsonObject node = node(nodeName);
            key(node, "position", frame, vec3(position));
            key(node, "orientation", frame, quat(orientation));
            key(node, "shapeStretch", frame, vec3(bone.scale));

            String bendTarget = HytaleRig.BEND_TARGETS.get(boneName);
            if (bendTarget != null) {
                key(node(bendTarget), "orientation", frame, quat(HytaleRig.bendToOrientation(bone.bend)));
            }
        }
    }

    /** Every animated node carries all five tracks, even the two an emote never fills. */
    private JsonObject node(String name) {
        JsonObject node = this.nodeAnimations.getAsJsonObject(name);
        if (node == null) {
            node = new JsonObject();
            for (String track : new String[]{"position", "orientation", "shapeStretch", "shapeVisible", "shapeUvOffset"}) {
                node.add(track, new JsonArray());
            }
            this.nodeAnimations.add(name, node);
        }
        return node;
    }

    /**
     * Appends a keyframe, collapsing a stretch where the node does not move down to the two keyframes that bound it.
     * <p>
     * The trailing one is not just a size saving: keyframes interpolate linearly, so a bone that holds still from frame
     * 0 to 99 and moves on 100 would drift across the whole span instead of staying put. Once two keyframes in a row
     * carry the same delta, the second is simply dragged forward to the current frame.
     */
    private static void key(JsonObject node, String track, int frame, JsonObject delta) {
        JsonArray keyframes = node.getAsJsonArray(track);
        int size = keyframes.size();

        if (size > 0 && delta.equals(keyframes.get(size - 1).getAsJsonObject().get("delta"))) {
            if (size > 1 && delta.equals(keyframes.get(size - 2).getAsJsonObject().get("delta"))) {
                keyframes.get(size - 1).getAsJsonObject().addProperty("time", frame); // extend the still run
                return;
            }
        }

        JsonObject keyframe = new JsonObject();
        keyframe.addProperty("time", frame);
        keyframe.add("delta", delta);
        keyframe.addProperty("interpolationType", "linear"); // the engine already applied the emote's own easing
        keyframes.add(keyframe);
    }

    private static JsonObject vec3(Vector3f value) {
        JsonObject delta = new JsonObject();
        delta.addProperty("x", value.x);
        delta.addProperty("y", value.y);
        delta.addProperty("z", value.z);
        return delta;
    }

    private static JsonObject quat(Quaternionf value) {
        JsonObject delta = new JsonObject();
        delta.addProperty("x", value.x);
        delta.addProperty("y", value.y);
        delta.addProperty("z", value.z);
        delta.addProperty("w", value.w);
        return delta;
    }
}
