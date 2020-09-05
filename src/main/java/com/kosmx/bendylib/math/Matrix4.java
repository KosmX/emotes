package com.kosmx.bendylib.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix4f;

public class Matrix4 extends Matrix4f {

    /**
     * create a matrix from eigen vectors.
     * The eigen values are the length of the vectors
     * (just to make things easier)
     */
    public void fromEigenVector(Vector3f vec1, Vector3f vec2, Vector3f vec3){
        this.a00 = vec1.getX();
        this.a10 = vec1.getY();
        this.a20 = vec1.getZ();
        this.a30 = 0;
        this.a01 = vec2.getX();
        this.a11 = vec2.getY();
        this.a21 = vec2.getZ();
        this.a31 = 0;
        this.a02 = vec3.getX();
        this.a12 = vec3.getY();
        this.a22 = vec3.getZ();
        this.a32 = 0;
        this.a03 = 0;
        this.a13 = 0;
        this.a23 = 0;
        this.a33 = 1;

        Matrix4f matrix4f = this.copy();

            float f = this.determinantAndAdjugate();
            if (Math.abs(f) > 1.0E-6F) {
                this.multiply(1/f);
            }
            else {
                this.loadIdentity();
                return;
            }
        this.multiply(Matrix4f.scale(length(vec1), length(vec2), length(vec3)));
        this.multiply(matrix4f);
    }

    public static float length(Vector4f vector4f){
        return vector4f.getX() * vector4f.getX() + vector4f.getY() * vector4f.getY() + vector4f.getZ() * vector4f.getZ() + vector4f.getW() * vector4f.getW();
    }
    public static float length(Vector3f vector4f){
        return vector4f.getX() * vector4f.getX() + vector4f.getY() * vector4f.getY() + vector4f.getZ() * vector4f.getZ();
    }
}
