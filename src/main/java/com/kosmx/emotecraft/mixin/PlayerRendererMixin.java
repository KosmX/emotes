package com.kosmx.emotecraft.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {


    @Inject(method = "setupTransforms", at = @At("RETURN"))
    private void setRotation(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float a, float bodyYaw, float tickDelta, CallbackInfo info){

    }
}
