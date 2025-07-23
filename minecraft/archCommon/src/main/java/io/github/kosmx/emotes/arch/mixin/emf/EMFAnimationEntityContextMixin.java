package io.github.kosmx.emotes.arch.mixin.emf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.utils.EMFEntity;

@Pseudo
@Mixin(value = EMFAnimationEntityContext.class, remap = false)
public class EMFAnimationEntityContextMixin {
    @Shadow
    private static EMFEntity IEMFEntity;

    @WrapMethod(method = "isEntityAnimPaused()Z", require = 0)
    private static boolean pauseAnimationsWhenEmoting(Operation<Boolean> original) {
        if (IEMFEntity == null || IEMFEntity instanceof UnsafeRemotePlayer) {
            return true;
        }
        if (IEMFEntity instanceof AbstractClientPlayer player && player.isPlayingEmote()) {
            return true;
        }
        return original.call();
    }
}
