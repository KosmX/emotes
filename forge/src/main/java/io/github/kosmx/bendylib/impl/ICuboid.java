package io.github.kosmx.bendylib.impl;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Minecraft Cuboid object
 * define it as you wish, render it as you wish!
 * You can use {@link BendableCuboid} to bend parts.
 */
public interface ICuboid {

    /**
     * See {@link BendableCuboid#render(PoseStack.Pose, VertexConsumer, float, float, float, float, int, int)} how to do it
     * Or you can check the original MC code {@link net.minecraft.client.model.geom.ModelPart#render(PoseStack, VertexConsumer, int, int, float, float, float, float)}
     *
     * @param matrices Minecraft's Matrix transformation
     * @param vertexConsumer Minecraft Vertex consumer, add vertices to render
     * @param red red
     * @param green green
     * @param blue blue
     * @param alpha alpha
     * @param light light
     * @param overlay overlay
     */
    void render(PoseStack.Pose matrices, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, int overlay);
}
