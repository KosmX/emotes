package io.github.kosmx.emotes.common.tools;

/**
 * Three-dimensional double vector
 */
public class Vec3d extends Vector3<Double> {

    public Vec3d(Double x, Double y, Double z) {
        super(x, y, z);
    }

    public double squaredDistanceTo(Vec3d vec3d){
        double a = this.x - vec3d.x;
        double b = this.y - vec3d.y;
        double c = this.z - vec3d.z;
        return a*a + b*b + c*c;
    }

    public double distanceTo(Vec3d vec3d){
        return Math.sqrt(squaredDistanceTo(vec3d));
    }
}
