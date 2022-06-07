package io.github.kosmx.bendylibForge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylibForge.ModelPartAccessor;
import io.github.kosmx.bendylibForge.MutableCuboid;
import io.github.kosmx.bendylibForge.impl.accessors.CuboidSideAccessor;
import io.github.kosmx.bendylibForge.impl.accessors.IModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class IModelPartMixin implements IModelPartAccessor {

    @Shadow @Final private Map<String, ModelPart> children;

    @Shadow @Final private List<ModelPart.Cube> cubes;

    @Shadow protected abstract void compile(PoseStack.Pose p_104291_, VertexConsumer p_104292_, int p_104293_, int p_104294_, float p_104295_, float p_104296_, float p_104297_, float p_104298_);

    private boolean hasMutatedCuboid = false;
    /**
     * VanillaDraw won't cause slowdown in vanilla and will fix many issues.
     * If needed, use {@link IModelPartAccessor#setWorkaround(ModelPartAccessor.Workaround)} to set the workaround function
     * {@link ModelPartAccessor.Workaround#None} to do nothing about it. It will work in Vanilla, but not with Sodium/OF
     */
    private ModelPartAccessor.Workaround workaround = ModelPartAccessor.Workaround.VanillaDraw;

    @Override
    public List<ModelPart.Cube> getCuboids() {
        hasMutatedCuboid = true;
        return cubes;
    }

    @Override
    public Map<String, ModelPart> getChildren() {
        return children;
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyTransformExtended(ModelPart part, CallbackInfo ci){
        if(((IModelPartAccessor)part).getCuboids() == null || cubes == null) return; // Not copying state
        Iterator<ModelPart.Cube> iterator0 = ((IModelPartAccessor)part).getCuboids().iterator();
        Iterator<ModelPart.Cube> iterator1 = cubes.iterator();

        while (iterator0.hasNext() && iterator1.hasNext()){
            MutableCuboid cuboid1 = (MutableCuboid) iterator1.next();
            MutableCuboid cuboid0 = (MutableCuboid) iterator0.next();
            cuboid1.copyStateFrom(cuboid0);
        }

    }

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void redirectRenderCuboids(ModelPart modelPart, PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha){
        if(workaround == ModelPartAccessor.Workaround.ExportQuads){
            for(ModelPart.Cube cuboid:cubes){
                ((CuboidSideAccessor)cuboid).doSideSwapping(); //:D
            }
            compile(entry, vertexConsumer, light, overlay, red, green, blue, alpha);
            for(ModelPart.Cube cuboid:cubes){
                ((CuboidSideAccessor)cuboid).resetSides(); //:D
            }
        }
        else if(workaround == ModelPartAccessor.Workaround.VanillaDraw){
            if(!hasMutatedCuboid || cubes.size() == 1 && ((MutableCuboid)cubes.get(0)).getActiveMutator() == null){
                compile(entry, vertexConsumer, light, overlay, red, green, blue, alpha);
            }
            else {
                for(ModelPart.Cube cuboid:cubes){
                    cuboid.compile(entry, vertexConsumer, light, overlay, red, green, blue, alpha);
                }
            }
        }
        else {
            compile(entry, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }

    @Override
    public void setWorkaround(ModelPartAccessor.Workaround workaround) {
        this.workaround = workaround;
    }
}
