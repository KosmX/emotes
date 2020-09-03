package com.kosmx.bendylib;

import com.kosmx.bendylib.objects.ICuboid;
import com.mojang.datafixers.kinds.IdF;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.Inject;

import javax.annotation.Nullable;

public abstract class MutableModelPart extends ModelPart {

    @Nullable
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
        this((int)modelPart.textureWidth, (int)modelPart.textureHeight, modelPart.textureOffsetU, modelPart.textureOffsetV);
    }

    /*
    @Override
    public Cuboid getRandomCuboid(Random random) {
        if(this.cuboids.size() != 0) return super.getRandomCuboid(random);
        else return new Cuboid()
    }

     *///TODO don't cause crash


    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        if(!iCuboids.isEmpty()){
            matrices.push();
            this.rotate(matrices);
            this.renderICuboids(matrices.peek(), vertices, light, overlay, red, green, blue, alpha);
            matrices.pop();
        }
    }

    protected void renderICuboids(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.iCuboids.forEach((cuboid)->{
            cuboid.render(matrices, vertexConsumer, red, green, blue, alpha, light, overlay);
        });
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

    /**
     * to restore the last part, when deactivated.
     */
    public void setLast(@Nullable MutableModelPart part){
        if(this == part){
            this.last = null;
            return;
        }
        this.last = part;
        if(this.loopPrevent())this.last = null;
    }

    public boolean remove(MutableModelPart part){
        if(this.last != null){
            if(this.last == part){
                MutableModelPart old = this.last;
                this.last = old.last;
                old.last = null;
                return true;
            }
            else {
                return this.last.remove(part);
            }
        }
        return false;
    }

    @Nullable
    public MutableModelPart getLast(){
        return this.last;
    }

    @Nullable
    public MutableModelPart getActive(){
        if(this.isActive()) return this;
        if(this.last != null)return this.last.getActive();
        return null;
    }

    /**
     * @return true if there is any loop.
     * If you need to override it and you know what are you doing, you will know how to do it.
     */
    public final boolean loopPrevent(){
        if(this.last != null){
            if(this.loopPrevent())return true;
            MutableModelPart part = this;
            while (part.last != null){
                part = part.last;
                if(part == this)return true;
            }
        }
        return false;
    }
}
