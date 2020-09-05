package com.kosmx.bendylib.objects;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Minecraft Cuboid object
 * define it as you wish, render it as you wish!
 * You can use {@link BendableCuboid} to bend parts.
 */
public interface ICuboid {

    /**
     * @param matrices Minecraft direction and location supplier
     * @param vertexConsumer Minecraft Vertex consumer, add vertices to render
     * @param red red
     * @param green green
     * @param blue blue
     * @param alpha alpha
     * @param light light
     * @param overlay overlay
     */
    void render(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, int overlay);
}
