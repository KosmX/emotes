package io.github.kosmx.emotes.hytale.bake;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

/**
 * Translates the Minecraft humanoid rig that Emotecraft animates onto Hytale's player skeleton
 * ({@code Common/Characters/Player.blockymodel}).
 * <p>
 * Both formats express a pose as a <i>delta from the rest pose</i>, so no rest transform has to be folded in here —
 * only the bone naming, the unit scale and the axis convention differ.
 */
public final class HytaleRig {
    private HytaleRig() {
    }

    /**
     * Translations are retargeted proportionally rather than by raw unit conversion.
     * <p>
     * Emotecraft animates in its own frame of 1/16-block units with the neck at y=24 (1.5 blocks); Hytale's rig uses
     * 1/32-block units with the head node at y=68 (2.125 blocks), so its character is markedly taller. Converting units
     * alone (a flat x2) would keep absolute distances but shrink every gesture relative to the body — a hand raised to
     * the head would stop short of it. Scaling by the two rigs' proportions keeps the pose's intent instead.
     */
    public static final float POSITION_SCALE = 68.0F / 24.0F;

    /**
     * Emotecraft bone -> Hytale node. Hytale splits the Minecraft torso into {@code Pelvis}/{@code Belly}/{@code Chest}
     * and each limb into three segments, so the Minecraft bone drives the topmost segment and its bend value drives the
     * joint below it (see {@link #BEND_TARGETS}).
     * <p>
     * Note that Hytale's {@code L-} nodes sit on +X and {@code R-} on -X, matching Minecraft's left/right.
     */
    public static final Map<String, String> BONES = Map.ofEntries(
            Map.entry("head", "Head"),
            // Emotecraft's "body" displaces the whole player, which in Hytale hangs off the pelvis
            Map.entry("body", "Pelvis"),
            Map.entry("torso", "Chest"),
            Map.entry("left_arm", "L-Arm"),
            Map.entry("right_arm", "R-Arm"),
            Map.entry("left_leg", "L-Thigh"),
            Map.entry("right_leg", "R-Thigh"),
            // Held items. Hytale parents these attachment nodes to the hands, so an item tracks the arm on its own;
            // mapping them as well lets an emote that poses the item relative to the hand keep doing so.
            Map.entry("left_item", "L-Attachment"),
            Map.entry("right_item", "R-Attachment"),
            // The cape rides Hytale's back slot, which hangs off the chest. Emotecraft's "elytra" bone is deliberately
            // left out: Hytale has no elytra and only this one node on the back, so mapping both would have the two
            // bones overwrite each other frame by frame.
            Map.entry("cape", "Back-Attachment")
    );

    /** Emotecraft authors the cape's pitch inverted relative to the rest of the rig. */
    public static final String CAPE_BONE = "cape";

    /**
     * Emotecraft encodes elbow/knee/waist flexing as a single {@code bend} scalar on the parent bone. Hytale has real
     * joints instead, so the bend is replayed as a pitch rotation on the segment below.
     */
    public static final Map<String, String> BEND_TARGETS = Map.of(
            "left_arm", "L-Forearm",
            "right_arm", "R-Forearm",
            "left_leg", "L-Calf",
            "right_leg", "R-Calf",
            "torso", "Belly"
    );

    /**
     * Both rigs are Y-up, so the difference is a half turn about Y: {@code (x, y, z) -> (-x, y, -z)}.
     * <p>
     * Emotecraft does not animate in Minecraft's Y-down {@code ModelPart} space — the engine's own bone table places
     * the origin at the feet with the head at y=+24, the right arm at x=+5 and the cape at z=+2, i.e. +X is the
     * character's right and +Z is behind them. Hytale mirrors both: its {@code R-} nodes sit on -X ({@code R-Shoulder}
     * at x=-14.5) and the back is at -Z ({@code Back-Attachment} at z=-18).
     */
    public static Vector3f toHytalePosition(Vector3f mcPosition) {
        return new Vector3f(
                -mcPosition.x * POSITION_SCALE,
                mcPosition.y * POSITION_SCALE,
                -mcPosition.z * POSITION_SCALE
        );
    }

    /**
     * Converts an Emotecraft euler rotation (radians) into the quaternion delta Hytale expects.
     * <p>
     * The triple is applied Z-Y-X, the order Minecraft composes part rotations in, and the same half turn about Y as
     * {@link #toHytalePosition} negates the two axes perpendicular to it.
     */
    public static Quaternionf toHytaleOrientation(Vector3f mcRotation) {
        return new Quaternionf().rotateZYX(-mcRotation.z, mcRotation.y, -mcRotation.x);
    }

    /** A bend flexes the joint about its own X axis, which the half turn about Y reverses. */
    public static Quaternionf bendToOrientation(float bend) {
        return new Quaternionf().rotateX(-bend);
    }

    /** The Emotecraft bone whose rotation moves the whole character rather than a limb. */
    public static final String ROOT_BONE = "body";

    /**
     * How far Emotecraft's root pivot sits from the node it drives, in the node's parent frame.
     * <p>
     * Emotecraft turns the whole character about the waist ({@code body} at y=12, i.e. y=34 once scaled), while
     * Hytale's {@code Pelvis} sits at y=51 — half a block apart, enough to visibly change a full-body lean.
     * <p>
     * Only the root gets this treatment. Every other joint has to keep turning about the target rig's own joint: the
     * Emotecraft pivots for {@code torso} and the limbs land 0.2-0.3 blocks away from the matching Hytale sockets
     * (Emotecraft's {@code torso} pivots at the neck, not the chest), so honouring them would swing each part out of
     * its socket and tear the skeleton apart. Differing proportions are retargeted, not reproduced.
     */
    public static final Vector3f ROOT_PIVOT_OFFSET = new Vector3f(0.0F, 34.0F - 51.0F, 0.0F);

    /**
     * The translation that turns a rotation about the node's own origin into one about a pivot {@code offset} away.
     * <p>
     * Rotating about a point C gives {@code X' = R*X + (C - R*C)}, while a node rotates about its own origin P and then
     * takes a translation T: {@code X' = R*X + (P - R*P) + T}. Equating the two leaves {@code T = (I - R)(C - P)}.
     */
    public static Vector3f pivotCompensation(Quaternionf rotation, Vector3f offset) {
        return new Vector3f(offset).sub(rotation.transform(new Vector3f(offset)));
    }
}
