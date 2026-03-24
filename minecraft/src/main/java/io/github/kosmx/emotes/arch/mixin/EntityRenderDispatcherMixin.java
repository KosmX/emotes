package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
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
                    "distanceToSqr"
            },
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void emotecraft$fixNPE(CallbackInfoReturnable<Double> cir, @Local(argsOnly = true) Entity entity) {
        if (this.camera == null || entity instanceof UnsafeMannequin) cir.setReturnValue(Double.MAX_VALUE);
    }
}
