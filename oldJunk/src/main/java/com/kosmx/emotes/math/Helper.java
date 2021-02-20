package com.kosmx.emotes.math;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Pair;

public class Helper {

    /**
     * This is for bending.
     * @param matrices Matrices
     * @param pair Pair
     */
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
