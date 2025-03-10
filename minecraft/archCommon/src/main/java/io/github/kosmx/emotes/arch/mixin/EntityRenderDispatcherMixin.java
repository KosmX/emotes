package io.github.kosmx.emotes.arch.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;

    @Inject(
            method = {
                    "distanceToSqr(Lnet/minecraft/world/entity/Entity;)D",
                    "distanceToSqr(DDD)D"
            },
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void emotecraft$fixNPE(CallbackInfoReturnable<Double> cir) {
        if (this.camera == null) cir.setReturnValue(Double.MAX_VALUE);
    }
}
