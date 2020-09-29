package com.kosmx.emotecraft.mixin;


import com.kosmx.emotecraft.mixinInterface.IUpperPartHelper;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    public ArmorRendererMixin(FeatureRendererContext<T, M> context){
        super(context);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void initInject(FeatureRendererContext<T, M> context, A leggingsModel, A bodyModel, CallbackInfo ci){
        ((IUpperPartHelper) this).setUpperPart(false);
    }
}
