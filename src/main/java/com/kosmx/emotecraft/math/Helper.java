package com.kosmx.emotecraft.math;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Pair;

public class Helper {
    public static int colorHelper(int r, int g, int b, int a){
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);  //Sometimes minecraft uses ints as color...
    }

    @Deprecated
    public static float clamp(float f){
        return  (float) (f%Math.PI);
    }

    public static void roteteMatrixStack(MatrixStack matrices, Pair<Float, Float> pair){
        float offset = 0.375f;
        matrices.translate(0, offset, 0);
        float bend = pair.getRight();
        float axisf = - pair.getLeft();
        Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
        //return this.setRotation(axis.getRadialQuaternion(bend));
        matrices.multiply(axis.getRadialQuaternion(bend));
        matrices.translate(0, - offset, 0);
    }
}
