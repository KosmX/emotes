package io.github.kosmx.emotes.arch.mixin.emf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.utils.EMFEntity;

@Pseudo
@Mixin(value = EMFAnimationEntityContext.class, remap = false)
public class EMFAnimationEntityContextMixin {

    @WrapMethod(method = "isEntityAnimPaused()Z", require = 0)
    private static boolean pauseAnimationsWhenEmoting(Operation<Boolean> original) {
        // use public getter rather than @Shadow as they can't (require = 0) to fail silently
        // works for EMF 3.0.0+ which is MC 1.20+
        EMFEntity entity = EMFAnimationEntityContext.getEmfState().emfEntity();
        if (entity == null || entity instanceof UnsafeRemotePlayer) {
            return true;
        }
        if (entity instanceof AbstractClientPlayer player && player.isPlayingEmote()) {
            return true;
        }
        return original.call();
    }
}
