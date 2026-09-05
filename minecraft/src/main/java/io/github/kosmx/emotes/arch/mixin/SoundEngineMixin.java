package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    // Only the pool choice: streaming has 8 channels shared with music, and ours streams out of memory
    @ModifyExpressionValue(
            method = "play",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/sounds/Sound;shouldStream()Z",
                    ordinal = 1
            )
    )
    private boolean emotecraft$takeAStaticChannel(boolean original, @Local(argsOnly = true) SoundInstance instance) {
        return original && !(instance instanceof EmoteSoundInstance);
    }

    @WrapOperation(
            method = "play",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private CompletableFuture<AudioStream> emotecraft$streamDecodedSound(SoundBufferLibrary library, Identifier location, boolean looping, Operation<CompletableFuture<AudioStream>> original, @Local(argsOnly = true) SoundInstance instance) {
        if (instance instanceof EmoteSoundInstance sound) {
            return CompletableFuture.completedFuture(sound.stream());
        }
        return original.call(library, location, looping);
    }
}
