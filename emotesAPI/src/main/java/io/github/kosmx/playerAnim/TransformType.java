package io.github.kosmx.playerAnim;

public enum TransformType {
    /**
     * The part is shifted in 3d space into the direction
     */
    POSITION,
    /**
     * The part is rotated in 3D space using Euler angles
     */
    ROTATION,
    /**
     * Bend the part, the vector should look like this: {bend planes rotation 0-2π, bend value, not defined}
     */
    BEND;
}
