package io.github.kosmx.emotes.arch.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.arch.executor.types.Key;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyMapping.class, priority = 2000)
public class KeyEventMixin {
    @Inject(method = "click", at = @At(value = "HEAD"))
    private static void keyPressCallback(InputConstants.Key key, CallbackInfo ci){
        EmoteHolder.handleKeyPress(new Key(key));
    }
}
