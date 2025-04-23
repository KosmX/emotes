package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundDirectInstance;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundEventInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @WrapOperation(
            method = "calculatePitch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
            )
    )
    private float emotecraft$extendOctaves(float value, float min, float max, Operation<Float> original, @Local(argsOnly = true) SoundInstance sound) {
        if (sound instanceof SoundEventInstance || sound instanceof SoundDirectInstance) {
            return value;
        }
        return original.call(value, min, max);
    }
}
