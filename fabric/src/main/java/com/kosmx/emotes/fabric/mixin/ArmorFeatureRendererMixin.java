package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initInject(FeatureRendererContext<T, M> context, A leggingsModel, A bodyModel, CallbackInfo ci){
        ((IUpperPartHelper)this).setUpperPart(false);
    }
}
