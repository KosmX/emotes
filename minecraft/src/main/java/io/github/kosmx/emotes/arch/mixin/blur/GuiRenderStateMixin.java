package io.github.kosmx.emotes.arch.mixin.blur;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.arch.gui.ducks.ModalBlurMixin;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin implements ModalBlurMixin {
    @Shadow
    private int firstStratumAfterBlur;

    @Shadow
    @Final
    private List<?> strata;

    @Unique
    private int emotecraft$firstStratumAfterModalBlur = Integer.MAX_VALUE;

    // A modal blurs once more, over the screen under it that blurred its own background
    @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"), cancellable = true)
    private void emotecraft$blurUnderModal(CallbackInfo ci) {
        if (this.firstStratumAfterBlur != Integer.MAX_VALUE && this.emotecraft$firstStratumAfterModalBlur == Integer.MAX_VALUE) {
            this.emotecraft$firstStratumAfterModalBlur = this.strata.size() - 1;
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "traverse(Ljava/util/function/Consumer;Lnet/minecraft/client/renderer/state/gui/GuiRenderState$TraverseRange;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0)
    )
    private int emotecraft$stopAtModalBlur(int size, @Local(argsOnly = true) GuiRenderState.TraverseRange range) {
        return range == GuiRenderState.TraverseRange.AFTER_BLUR ? Math.min(size, this.emotecraft$firstStratumAfterModalBlur) : size;
    }

    @Override
    public boolean emotecraft$passScreenBlur() {
        if (this.emotecraft$firstStratumAfterModalBlur == Integer.MAX_VALUE) return false;
        this.firstStratumAfterBlur = this.emotecraft$firstStratumAfterModalBlur;
        this.emotecraft$firstStratumAfterModalBlur = Integer.MAX_VALUE;
        return true;
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void emotecraft$resetModalBlur(CallbackInfo ci) {
        this.emotecraft$firstStratumAfterModalBlur = Integer.MAX_VALUE;
    }
}
