package io.github.kosmx.emotes.arch.mixin;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.github.kosmx.emotes.arch.screen.utils.UnsafePlayerRenderState;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PictureInPictureRenderer.class)
public abstract class PictureInPictureRendererMixin {
    @Shadow
    private @Nullable GpuTextureView textureView;
    @Shadow
    private @Nullable GpuTextureView depthTextureView;
    @Shadow
    private @Nullable GpuTexture texture;
    @Shadow
    private @Nullable GpuTexture depthTexture;

    @Inject(
            method = "blitTexture",
            at = @At(
                    value = "RETURN"
            )
    )
    private void emotecraft$fixRender(PictureInPictureRenderState renderState, GuiRenderState guiRenderState, CallbackInfo ci) {
        if (renderState instanceof GuiEntityRenderState state && state.renderState() instanceof UnsafePlayerRenderState) {
            this.texture = null; // don't close
            this.textureView = null; // don't close

            if (this.depthTexture != null) {
                this.depthTexture.close();
                this.depthTexture = null;
            }
            if (this.depthTextureView != null) {
                this.depthTextureView.close();
                this.depthTextureView = null;
            }
        }
    }
}
