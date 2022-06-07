package io.github.kosmx.emotes.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.emotes.fabric.BendableModelPart;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Compatibility issue: can not redirect {@link RenderLayer#render(PoseStack, MultiBufferSource, int, Entity, float, float, float, float, float, float)}
 * I have to modify the matrixStack and do not forget to POP it!
 *
 * I can inject into the enhanced for
 * {@link List#iterator()}      //initial push to keep in sync
 * {@link Iterator#hasNext()}   //to pop the matrix stack
 * {@link Iterator#next()}      //I can see the modelPart, decide if I need to manipulate it. But push always
 *
 * @param <T>
 * @param <M>
 */
@SuppressWarnings("unchecked")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderRedirect<T extends Entity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRenderRedirect(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private void initialPush(LivingEntity livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        poseStack.pushPose();
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private void popMatrixStack(LivingEntity livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        poseStack.popPose();
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private Object transformMatrixStack(Iterator<RenderLayer<T, M>> instance, LivingEntity livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i){
        poseStack.pushPose();
        RenderLayer<T, M> layer = instance.next();
        if(livingEntity instanceof Player && livingEntity instanceof IEmotePlayerEntity && ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).getAnimation().isActive() && ((IUpperPartHelper) layer).isUpperPart()){
            BendableModelPart.roteteMatrixStack(poseStack, ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).getAnimation().getBend("body"));
        }
        return layer;
    }
}
