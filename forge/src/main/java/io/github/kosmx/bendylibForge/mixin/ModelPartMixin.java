package io.github.kosmx.bendylibForge.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylibForge.IModelPart;
import io.github.kosmx.bendylibForge.MutableModelPart;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements IModelPart {

    @Shadow public boolean visible;

    @Shadow public float xTexSize;
    @Shadow public float yTexSize;
    @Shadow public int xTexOffs;
    @Shadow public int yTexOffs;

    @Shadow public abstract void translateAndRotate(PoseStack arg);

    protected final ObjectList<MutableModelPart> mutatedParts = new ObjectArrayList<>();

    /**
     * @author KosmX - bendy-lib
     * @param part part, what ypu want to use
     * @return true, if it isn't assigned already.
     */
    @Override
    public boolean mutate(MutableModelPart part) {
        if(mutatedParts.contains(part))return false;
        mutatedParts.add(part);
        return true;
    }

    /**
     * @param part remove, if this is the active mutated.
     * @return is the action success
     */
    @Override
    public boolean removeMutate(MutableModelPart part) {
        return mutatedParts.remove(part);
    }


    /**
     * @author KosmX - bendy-lib
     * @return Active, highest priority mutated part, null if no active or empty
     */
    @Nullable
    @Override
    public MutableModelPart getActiveMutatedPart() {
        MutableModelPart part = null;
        for(MutableModelPart i:this.mutatedParts){
            if(i.isActive() && (part == null || !part.isActive() || part.getPriority() <= i.getPriority())){
                if(part != null && part.getPriority() == i.getPriority()){
                    System.out.println("[bendy-lib] " + part.modId() + " is possibly incompatible with " + i.modId() + ".");
                    return null;
                }else {
                    part = i;
                }
            }
        }
        return part;
    }

    @Override
    public float getTextureWidth() {
        return this.xTexSize;
    }

    @Override
    public float getTextureHeight() {
        return this.yTexSize;
    }

    @Override
    public int getU() {
        return this.xTexOffs;
    }

    @Override
    public int getV() {
        return this.yTexOffs;
    }

    /**
     * modified render function. it will be inherited.
     */
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD" ), cancellable = true)
    private void renderInject(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo callbackInfo){
        MutableModelPart mutatedPart = this.getActiveMutatedPart();
        if(this.visible && mutatedPart != null){
            matrices.pushPose();
            this.translateAndRotate(matrices);
            mutatedPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
            matrices.popPose();
            callbackInfo.cancel(); //if mutate active, don't render the original
        }
    }

}
