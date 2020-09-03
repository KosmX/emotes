package com.kosmx.bendylib;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import java.util.function.Supplier;


/**
 * ModelPart what is able to render BendableCuboids.
 * never finished class. Don't use it
 * use {@link MutableModelPart}
 * How-to
 */
@Deprecated
public class BendableModelPart extends MutableModelPart {
    protected final ObjectList<BendableCuboid> bendableCuboids = new ObjectArrayList<>();

    /**
     * Create a new Bendable part
     * If using it for Changing default model you should use
     * {@link BendableModelPart#of(ModelPart)}
     * to copy the parameters from an existing ModelPart
     * @param textureWidth with of the texture
     * @param textureHeight height of the texture
     * @param textureOffsetU texture U offset
     * @param textureOffsetV texture V offset
     */
    public BendableModelPart(int textureWidth, int textureHeight, int textureOffsetU, int textureOffsetV) {
        super(textureWidth, textureHeight, textureOffsetU, textureOffsetV);
    }

    @Override
    public String modId() {
        return "bendy-lib"; //TODO
    }

    /**
     * Copy the texture parameters from an existing ModelPart.
     * @param modelPart ModelPart
     * @return return a new {@link BendableModelPart}
     */
    public static BendableModelPart of(ModelPart modelPart){
        return new BendableModelPart((int)modelPart.textureWidth, (int)modelPart.textureHeight, modelPart.textureOffsetU, modelPart.textureOffsetV);
    }

    /*
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if (this.visible) {
            if (!this.cuboids.isEmpty() || !this.children.isEmpty()) {
                matrices.push();
                this.rotate(matrices);
                this.renderCuboids(matrices.peek(), vertices, light, overlay, red, green, blue, alpha);

                this.renderBendableCuboids(matrices.peek(), vertices, light, overlay, red, green, blue, alpha);

                for (ModelPart modelPart : this.children) {
                    modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }

                matrices.pop();
            }
        }
    }

     */

    /*protected void renderBendableCuboids(MatrixStack.Entry matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = matrices.getModel();
        Matrix3f matrix3f = matrices.getNormal();

        for (BendableCuboid cuboid : this.bendableCuboids) {
            Quad[] var13 = cuboid.sides;

            for (Quad quad : var13) {
                Vector3f vector3f = quad.direction.copy();
                vector3f.transform(matrix3f);
                float f = vector3f.getX();
                float g = vector3f.getY();
                float h = vector3f.getZ();

                for (int i = 0; i < 4; ++i) {
                    Vertex vertex = quad.vertices[i];
                    float j = vertex.pos.getX() / 16.0F;
                    float k = vertex.pos.getY() / 16.0F;
                    float l = vertex.pos.getZ() / 16.0F;
                    Vector4f vector4f = new Vector4f(j, k, l, 1.0F);
                    vector4f.transform(matrix4f);
                    vertices.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, vertex.u, vertex.v, overlay, light, f, g, h);
                }
            }
        }

    }

     */

    public static class RepositionableVertex{
        public final Supplier<Vector3f> pos;
        public final float u;
        public final float v;
        public RepositionableVertex(Supplier<Vector3f> pos, float u, float v){
            this.pos = pos;
            this.u = u;
            this.v = v;
        }
        public Vector3f getPos(){
            return this.pos.get();
        }
    }


    /**
     * A Cuboid, what is able to blend.
     */
    public static class BendableCuboid{
        //protected Quad[] sides;
        //TODO math...
    }
}
