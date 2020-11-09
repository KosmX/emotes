package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.KeyPressCallback;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(KeyBinding.class)
public class KeyEventMixin {
    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"))
    private static void keyCallback(InputUtil.Key key, CallbackInfo ci){
        KeyPressCallback.EVENT.invoker().onKeyPress(key);
    }
}
