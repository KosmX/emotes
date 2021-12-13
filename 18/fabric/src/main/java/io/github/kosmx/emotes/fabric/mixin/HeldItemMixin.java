package io.github.kosmx.emotes.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(ItemInHandLayer.class)
public class HeldItemMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V", ordinal = 0))
    private void renderMixin(LivingEntity livingEntity, ItemStack stack, ItemTransforms.TransformType transformationMode, HumanoidArm arm, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci){
        if(livingEntity instanceof IEmotePlayerEntity){
            IEmotePlayerEntity<EmotePlayImpl> player = (IEmotePlayerEntity<EmotePlayImpl>) livingEntity;
            if(EmotePlayImpl.isRunningEmote(player.getEmote())){
                EmotePlayImpl emote = player.getEmote();

                Pair<Float, Float> pair = arm == HumanoidArm.LEFT ? emote.leftArm.getBend() : emote.rightArm.getBend();

                float offset = 0.25f;
                matrices.translate(0, offset, 0);
                float bend = pair.getRight();
                float axisf = - pair.getLeft();
                Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
                //return this.setRotation(axis.getRadialQuaternion(bend));
                matrices.mulPose(axis.rotation(bend));
                matrices.translate(0, - offset, 0);
            }
        }
    }

}
