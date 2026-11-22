package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    // Only the pool choice: streaming has 8 channels shared with music, and ours streams out of memory
    @ModifyExpressionValue(
            method = {
                    "play",

                    // NeoForge https://github.com/neoforged/NeoForge/blob/d90c085/patches/net/minecraft/client/sounds/SoundEngine.java.patch#L15
                    "playWithoutEvent"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/sounds/Sound;shouldStream()Z",
                    ordinal = 1
            )
    )
    private boolean emotecraft$takeAStaticChannel(boolean original, @Local(argsOnly = true) SoundInstance instance) {
        return original && !(instance instanceof EmoteSoundInstance);
    }
}
