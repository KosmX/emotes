package io.github.kosmx.bendylibForge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylibForge.impl.ICuboid;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * ModelPart to support ICuboids
 *
 * If you want to mutate existing Cuboids, see {@link ModelPartAccessor} and {@link MutableCuboid}
 *
 * This can be used with {@link ICuboid}.
 */
public abstract class MutableModelPart extends ModelPart {

    @Nullable
    @Deprecated
    private MutableModelPart last = null;

    protected final ObjectList<ICuboid> iCuboids = new ObjectArrayList<>();

    public MutableModelPart(List<Cube> cuboids, Map<String, ModelPart> children) {
        super(cuboids, children);
    }


    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        if(!iCuboids.isEmpty()){
            matrices.pushPose();
            this.translateAndRotate(matrices);
            this.renderICuboids(matrices.last(), vertices, light, overlay, red, green, blue, alpha);
            matrices.popPose();
        }
    }

    protected void renderICuboids(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.iCuboids.forEach((cuboid)-> cuboid.render(matrices, vertexConsumer, red, green, blue, alpha, light, overlay));
    }

    public void addICuboid(ICuboid cuboid){
        this.iCuboids.add(cuboid);
    }

}
