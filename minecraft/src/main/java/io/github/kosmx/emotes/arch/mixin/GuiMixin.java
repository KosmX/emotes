package io.github.kosmx.emotes.arch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.arch.screen.components.EmoteSubScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Screen.removed() doesn't say which screen comes next; an emote screen keeps its list under a library modal. */
@Mixin(Gui.class)
public abstract class GuiMixin {
    // At the removal itself: before it, NeoForge may still cancel or redirect the change, or clear its screen layers
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private void emotecraft$onScreenRemoved(CallbackInfo ci, @Local(argsOnly = true) @Nullable Screen next) {
        EmoteSubScreen.onScreenChange(((Gui) (Object) this).screen(), next);
    }
}
