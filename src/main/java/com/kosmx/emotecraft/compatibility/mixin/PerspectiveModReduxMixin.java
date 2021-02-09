package com.kosmx.emotecraft.compatibility.mixin;

import com.kosmx.emotecraft.compatibility.perspectiveRedux.HackedKeyBinding;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pm.c7.perspective.PerspectiveMod;

@Mixin(PerspectiveMod.class)
public class PerspectiveModReduxMixin {
    @Shadow @Final private static String TOGGLE_KEYBIND;

    @Shadow @Final private static String KEYBIND_CATEGORY;

    @Shadow private static KeyBinding toggleKey;

    @Redirect(method = "onInitializeClient", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/keybinding/KeyBindingRegistryImpl;registerKeyBinding"), remap = false)
    private KeyBinding redirect(KeyBinding binding){
        //And set the hacked stuff up.
        return KeyBindingRegistryImpl.registerKeyBinding( toggleKey = new HackedKeyBinding(TOGGLE_KEYBIND, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, KEYBIND_CATEGORY));
        //return null;
    }
}
