package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void emotecraft$shouldShowName(LivingEntity livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof UnsafeMannequin) cir.setReturnValue(false);
    }
}
