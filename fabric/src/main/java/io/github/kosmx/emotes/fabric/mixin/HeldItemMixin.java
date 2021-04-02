package io.github.kosmx.emotes.fabric.mixin;

import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.fabric.emote.EmotePlayImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemMixin {

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lnet/minecraft/util/math/Quaternion;)V", ordinal = 0))
    private void renderMixin(LivingEntity livingEntity, ItemStack stack, ModelTransformation.Mode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(livingEntity instanceof IEmotePlayerEntity){
            IEmotePlayerEntity<EmotePlayImpl> player = (IEmotePlayerEntity<EmotePlayImpl>) livingEntity;
            if(EmotePlayImpl.isRunningEmote(player.getEmote())){
                EmotePlayImpl emote = player.getEmote();

                Pair<Float, Float> pair = arm == Arm.LEFT ? emote.leftArm.getBend() : emote.rightArm.getBend();

                float offset = 0.25f;
                matrices.translate(0, offset, 0);
                float bend = pair.getRight();
                float axisf = - pair.getLeft();
                Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
                //return this.setRotation(axis.getRadialQuaternion(bend));
                matrices.multiply(axis.getRadialQuaternion(bend));
                matrices.translate(0, - offset, 0);
            }
        }
    }

}
