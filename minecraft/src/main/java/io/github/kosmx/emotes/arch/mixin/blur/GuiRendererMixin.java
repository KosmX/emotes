package io.github.kosmx.emotes.arch.mixin.blur;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import io.github.kosmx.emotes.arch.gui.blur.ModalBlur;
import io.github.kosmx.emotes.arch.gui.ducks.ModalBlurMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    private GuiRenderState renderState;

    @Shadow
    @Final
    private List<?> draws;

    @Unique
    private final ModalBlur emotecraft$modalBlur = new ModalBlur();

    @Unique
    private int emotecraft$firstDrawIndexAfterModalBlur = Integer.MAX_VALUE;

    // Meshed apart, the elements after a modal's blur can be drawn after it
    @WrapOperation(
            method = "prepare",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;addElementsToMeshes(Lnet/minecraft/client/renderer/state/gui/GuiRenderState$TraverseRange;)V", ordinal = 1)
    )
    private void emotecraft$meshAfterModalBlur(GuiRenderer renderer, GuiRenderState.TraverseRange range, Operation<Void> original) {
        original.call(renderer, range);
        this.emotecraft$firstDrawIndexAfterModalBlur = Integer.MAX_VALUE;
        if (((ModalBlurMixin) this.renderState).emotecraft$passScreenBlur()) {
            this.emotecraft$firstDrawIndexAfterModalBlur = this.draws.size();
            original.call(renderer, range);
        }
    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;processBlurEffect()V"))
    private void emotecraft$blurUnderModal(GameRenderer gameRenderer, Operation<Void> original) {
        if (this.emotecraft$firstDrawIndexAfterModalBlur != Integer.MAX_VALUE) {
            this.emotecraft$modalBlur.blurUnder(gameRenderer);
        } else if (ModalBlur.progress() < 1.0F) {
            this.emotecraft$modalBlur.blurAlone(gameRenderer);
        } else {
            original.call(gameRenderer);
        }
    }

    @WrapOperation(
            method = "draw",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;II)V", ordinal = 1)
    )
    private void emotecraft$blurUnderModalWindow(GuiRenderer renderer, Supplier<String> label, RenderTarget target, GpuBufferSlice transforms, int start, int end, Operation<Void> original) {
        int modalStart = Math.min(this.emotecraft$firstDrawIndexAfterModalBlur, end);
        original.call(renderer, label, target, transforms, start, modalStart);
        if (modalStart < end) {
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(target.getDepthTexture(), 0.0);
            this.emotecraft$modalBlur.blurOver(Minecraft.getInstance().gameRenderer);
            original.call(renderer, label, target, transforms, modalStart, end);
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void emotecraft$closeModalBlur(CallbackInfo ci) {
        this.emotecraft$modalBlur.close();
    }
}
