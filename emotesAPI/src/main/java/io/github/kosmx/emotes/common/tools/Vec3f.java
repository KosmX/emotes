package io.github.kosmx.emotes.common.tools;

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

    public double distanceTo(Vec3d vec3d){
        return Math.sqrt(squaredDistanceTo(vec3d));
    }
}
