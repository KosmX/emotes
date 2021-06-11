package io.github.kosmx.emotes.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.kosmx.emotes.common.tools.Vector3;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "setupRotations", at = @At("RETURN"))
    private void applyBodyTransforms(AbstractClientPlayer abstractClientPlayerEntity, PoseStack matrixStack, float f, float bodyYaw, float tickDelta, CallbackInfo ci){
        if(((IEmotePlayerEntity<EmotePlayImpl>)abstractClientPlayerEntity).isPlayingEmote()){
            EmotePlayImpl emote = ((IEmotePlayerEntity<EmotePlayImpl>) abstractClientPlayerEntity).getEmote();
            emote.setTickDelta(tickDelta);

            Vector3<Double> vec3d = emote.torso.getBodyOffset();
            matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
            Vector3<Float> vec3f = emote.torso.getBodyRotation();
            matrixStack.mulPose(Vector3f.ZP.rotation(vec3f.getZ()));    //roll
            matrixStack.mulPose(Vector3f.YP.rotation(vec3f.getY()));    //pitch
            matrixStack.mulPose(Vector3f.XP.rotation(vec3f.getX()));    //yaw
            matrixStack.translate(0, - 0.7d, 0);
        }
    }
}
