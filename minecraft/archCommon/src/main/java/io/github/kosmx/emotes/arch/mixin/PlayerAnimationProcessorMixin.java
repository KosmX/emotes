package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zigythebird.playeranim.animation.PlayerAnimationProcessor;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAnimationProcessor.class)
public class PlayerAnimationProcessorMixin {
    @Shadow
    @Final
    private AbstractClientPlayer player;

    @WrapOperation(
            method = "handleAnimations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;isPaused()Z"
            )
    )
    private boolean emotecraft$unpause(Minecraft instance, Operation<Boolean> original) {
        if (this.player instanceof UnsafeRemotePlayer) return false;
        return original.call(instance);
    }
}
