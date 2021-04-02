package io.github.kosmx.emotes.fabric.mixin;

import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.emotes.fabric.BendableModelPart;
import io.github.kosmx.emotes.fabric.emote.EmotePlayImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unchecked")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderRedirect<T extends Entity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    protected LivingEntityRenderRedirect(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private void featureRendererTransformer(FeatureRenderer<T, M> featureRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch){
        if(livingEntity instanceof PlayerEntity && livingEntity instanceof IEmotePlayerEntity && ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).isPlayingEmote() && ((IUpperPartHelper) featureRenderer).isUpperPart()){
            matrices.push();
            BendableModelPart.roteteMatrixStack(matrices, ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).getEmote().torso.getBend());
            featureRenderer.render(matrices, vertexConsumers, light, livingEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            matrices.pop();
        }else{
            featureRenderer.render(matrices, vertexConsumers, light, livingEntity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        }
    }

}
