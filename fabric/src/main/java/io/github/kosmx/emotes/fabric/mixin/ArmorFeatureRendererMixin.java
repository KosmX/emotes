package io.github.kosmx.emotes.fabric.mixin;

import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initInject(RenderLayerParent<T, M> context, A leggingsModel, A bodyModel, CallbackInfo ci){
        ((IUpperPartHelper)this).setUpperPart(false);
    }
}
