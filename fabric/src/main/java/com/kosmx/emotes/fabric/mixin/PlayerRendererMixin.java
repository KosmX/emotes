package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.common.tools.Vector3;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.fabric.emote.EmotePlayImpl;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "setupTransforms", at = @At("RETURN"))
    private void applyBodyTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float bodyYaw, float tickDelta, CallbackInfo ci){
        if(((IEmotePlayerEntity<EmotePlayImpl>)abstractClientPlayerEntity).isPlayingEmote()){
            EmotePlayImpl emote = ((IEmotePlayerEntity<EmotePlayImpl>) abstractClientPlayerEntity).getEmote();
            emote.setTickDelta(tickDelta);

            Vector3<Double> vec3d = emote.torso.getBodyOffset();
            matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
            Vector3<Float> vec3f = emote.torso.getBodyRotation();
            matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(vec3f.getZ()));    //roll
            matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(vec3f.getY()));    //pitch
            matrixStack.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(vec3f.getX()));    //yaw
            matrixStack.translate(0, - 0.7d, 0);
        }
    }
}
