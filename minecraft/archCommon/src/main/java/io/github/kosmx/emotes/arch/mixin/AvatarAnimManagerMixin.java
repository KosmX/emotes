package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarAnimManager.class)
public class AvatarAnimManagerMixin {
    @Shadow
    @Final
    private Avatar avatar;

    @WrapOperation(
            method = "handleAnimations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;isPaused()Z"
            )
    )
    private boolean emotecraft$unpause(Minecraft instance, Operation<Boolean> original) {
        if (this.avatar instanceof UnsafeMannequin) return false;
        return original.call(instance);
    }
}
