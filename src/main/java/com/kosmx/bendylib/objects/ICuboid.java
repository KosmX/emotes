package com.kosmx.bendylib.objects;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * define it as you wish, render it as you wish
 */
public interface ICuboid {

    /**
     *
     * @param matrices Minecraft direction and location supplier
     * @param vertexConsumer
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @param light
     * @param overlay
     */
    void render(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, int overlay);
}
