package com.kosmx.bendylib.mixin;


import com.kosmx.bendylib.IModelPart;
import com.kosmx.bendylib.MutableModelPart;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IModelPart {

    @Shadow public boolean visible;

    @Shadow
    public void rotate(MatrixStack matrix) {}

    @Nullable
    protected MutableModelPart mutatedPart = null;

    /**
     * @author KosmX - bendy-lib
     * @param part part, what ypu want to use
     * @return is the mutation success, can be false if another mutation is already active
     */
    @Override
    public boolean mutate(MutableModelPart part) {
        if(part == this.mutatedPart)return false;
        if(this.mutatedPart != null && this.mutatedPart.isActive() && this.mutatedPart.getPriority() < part.getPriority())return false;
        if(this.mutatedPart != null && this.mutatedPart.getPriority() == part.getPriority()){
            System.out.println("[bendy-lib] " + part.modId() + " is possibly incompatible with " + this.mutatedPart.modId() + ".");
        }
        part.setLast(this.mutatedPart);
        this.mutatedPart = part;
        return true;
    }

    /**
     * @param part remove, if this is the active mutated.
     * @return is the action success
     */
    @Override
    public boolean removeMutate(MutableModelPart part) {
        if (part == null) part = this.mutatedPart;
        if(this.mutatedPart == part && this.mutatedPart != null){
            MutableModelPart old = this.mutatedPart;
            this.mutatedPart = this.mutatedPart.getLast();
            old.setLast(null);
            return true;
        }
        if(this.mutatedPart != null){
            return this.mutatedPart.remove(part);
        }
        return false;
    }


    /**
     * @author KosmX - bendy-lib
     */
    @Nullable
    @Override
    public MutableModelPart getMutatedPart() {
        return this.mutatedPart;
    }

    /**
     * modified render function. it will be inherited.
     */
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD" ), cancellable = true)
    private void renderInject(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo callbackInfo){
        if(this.visible && this.mutatedPart != null && this.mutatedPart.getActive() != null){
            matrices.push();
            this.rotate(matrices);
            this.mutatedPart.getActive().render(matrices, vertices, light, overlay, red, green, blue, alpha);
            matrices.pop();
            callbackInfo.cancel(); //if mutate active, don't render the original
        }
    }

}
