package io.github.kosmx.emotes.common.tools;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Vec3f extends Vector3<Float> {

    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);

    public Vec3f(float x, float y, float z) {
        super(x, y, z);
    }

    public double squaredDistanceTo(Vec3d vec3d){
        double a = this.x - vec3d.x;
        double b = this.y - vec3d.y;
        double c = this.z - vec3d.z;
        return a*a + b*b + c*c;
    }

    /**
     * Scale the vector
     * @param scalar scalar
     * @return scaled vector
     */
    public Vec3f scale(float scalar) {
        return new Vec3f(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar);
    }

    /**
     * Add two vectors
     * @param other other vector
     * @return sum vector
     */
    public Vec3f add(Vec3f other) {
        return new Vec3f(this.getX() + other.getX(), this.getY() + other.getY(), this.getZ() + other.getZ());
    }

    /**
     * Dot product with other vector
     * @param other rhs operand
     * @return v
     */
    public float dotProduct(Vec3f other) {
        return this.getX() * other.getX() + this.getY() * other.getY() + this.getZ() * other.getZ();
    }

    /**
     * Cross product
     * @param other rhs operand
     * @return v
     */
    public Vec3f crossProduct(Vec3f other) {
        return new Vec3f(
                this.getY()*other.getZ() - this.getZ()*other.getY(),
                this.getZ()*other.getX() - this.getX()*other.getZ(),
                this.getX()*other.getY() - this.getY()*other.getX()
        );
    }

    /**
     * Subtract a vector from this
     * @param rhs rhs operand
     * @return v
     */
    public Vec3f subtract(Vec3f rhs) {
        //You could have guessed what will happen here.
        return add(rhs.scale(-1));
    }

    public double distanceTo(Vec3d vec3d){
        return Math.sqrt(squaredDistanceTo(vec3d));
    }
}
