package io.github.kosmx.bendylib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.bendylib.impl.ICuboid;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

/**
 * You can use this to swap a ModelPart to something else.
 * {@link IModelPart#mutate(MutableModelPart)} to do that
 * ((IModelPart)yourModelPart).mutate(yourMutatedModelPart) will do the trick
 *
 * {@link IModelPart#removeMutate(MutableModelPart)} to remove
 * You can use is as the default modelPart in a model.
 * This can be used with {@link ICuboid}.
 */
public abstract class MutableModelPart extends ModelPart {

    @Nullable
    @Deprecated
    private MutableModelPart last = null;

    protected final ObjectList<ICuboid> iCuboids = new ObjectArrayList<>();
    public MutableModelPart(Model model) {
        super(model);
    }

    public MutableModelPart(Model model, int textureOffsetU, int textureOffsetV) {
        super(model, textureOffsetU, textureOffsetV);
    }

    public MutableModelPart(int textureWidth, int textureHeight, int textureOffsetU, int textureOffsetV) {
        super(textureWidth, textureHeight, textureOffsetU, textureOffsetV);
    }

    public MutableModelPart(ModelPart modelPart){
        this((int)modelPart.xTexSize, (int)modelPart.yTexSize, modelPart.xTexOffs, modelPart.yTexOffs);
    }

    /*
    @Override
    public Cuboid getRandomCuboid(Random random) {
        if(this.cuboids.size() != 0) return super.getRandomCuboid(random);
        else return new Cuboid()
    }

     *///TODO don't cause crash



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

    /**
     * For Cross-mod compatibility
     * @return the Priority level. If there is a lower level, that will be applied
     * Mods like Mo'bends should use higher e.g. 5
     * Mods like Emotecraft should use lover e.g. 1
     */
    public int getPriority(){
        return 2;
    }

    public boolean isActive(){
        return true;
    }

    /**
     * incompatibility finder tool
     * @return the mod's name or id
     */
    public abstract String modId();


    //The Bendable cuboid generator code
    public BendableCuboid createCuboid(int x, int y, int z, int sizeX, int sizeY, int sizeZ, float extraX, float extraY, float extraZ, Direction direction){
        BendableCuboid.Builder builder = new BendableCuboid.Builder();
        builder.x = x;
        builder.y = y;
        builder.z = z;
        builder.sizeX = sizeX;
        builder.sizeY = sizeY;
        builder.sizeZ = sizeZ;
        builder.extraX = extraX;
        builder.extraY = extraY;
        builder.extraZ = extraZ;
        builder.direction = direction;
        builder.textureWidth = (int) this.xTexSize;
        builder.textureHeight = (int) this.yTexSize;
        builder.u = xTexOffs;
        builder.v = yTexOffs;
        return builder.build();
    }
    public MutableModelPart addCuboid(int x, int y, int z, int sizeX, int sizeY, int sizeZ, float extraX, float extraY, float extraZ, Direction direction){
        this.iCuboids.add(this.createCuboid(x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, direction));
        return this;
    }

    public BendableCuboid createCuboid(int x, int y, int z, int sizeX, int sizeY, int sizeZ, float extra, Direction direction){
        return this.createCuboid(x, y, z, sizeX, sizeY, sizeZ, extra, extra, extra, direction);
    }
    public MutableModelPart addCuboid(int x, int y, int z, int sizeX, int sizeY, int sizeZ, float extra, Direction direction){
        return this.addCuboid(x, y, z, sizeX, sizeY, sizeZ, extra, extra, extra, direction);
    }
}
