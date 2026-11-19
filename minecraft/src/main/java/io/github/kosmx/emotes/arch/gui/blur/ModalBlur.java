package io.github.kosmx.emotes.arch.gui.blur;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/** The menu blur under a modal, split in two: the screen's own over its background, then the modal's over the screen, together as strong as the option. */
public final class ModalBlur implements AutoCloseable {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
            .withVertexShader("core/screenquad")
            .withFragmentShader(McUtils.newIdentifier("post/box_blur"))
            .withLocation(McUtils.newIdentifier("pipeline/box_blur"))
            .withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                    .withUniform("BlurConfig", UniformType.UNIFORM_BUFFER)
                    .build()
            )
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();
    private static final int ITERATIONS = 3; // As vanilla's blur post chain

    private final Uniforms under = new Uniforms("under");
    private final Uniforms over = new Uniforms("over");
    private @Nullable RenderTarget swap;
    private float overRadius;

    public static float progress() {
        return Minecraft.getInstance().gui.screen() instanceof BaseModalScreen modal ? modal.blurProgress() : 1.0F;
    }

    public void blurUnder(GameRenderer gameRenderer) {
        int option = gameRenderer.gameRenderState().optionsRenderState.menuBackgroundBlurriness;
        this.overRadius = option * progress();
        if (this.overRadius < option) {
            blur(gameRenderer.mainRenderTarget(), this.under, radiusOf(variance(option) - variance(this.overRadius), option));
        }
    }

    /** The frame's only blur, a modal's own over a screen that didn't blur. */
    public void blurAlone(GameRenderer gameRenderer) {
        this.overRadius = gameRenderer.gameRenderState().optionsRenderState.menuBackgroundBlurriness * progress();
        blurOver(gameRenderer);
    }

    public void blurOver(GameRenderer gameRenderer) {
        if (this.overRadius == gameRenderer.gameRenderState().optionsRenderState.menuBackgroundBlurriness) {
            gameRenderer.processBlurEffect();
        } else if (this.overRadius > 0.0F) {
            blur(gameRenderer.mainRenderTarget(), this.over, this.overRadius);
        }
    }

    private void blur(RenderTarget target, Uniforms uniforms, float radius) {
        RenderTarget swap = swapFor(target);
        uniforms.write(target, radius);
        GpuBuffer info = uniforms.info.currentBuffer();
        for (int i = 0; i < ITERATIONS; i++) {
            pass(target, swap, info, uniforms.horizontal.currentBuffer());
            pass(swap, target, info, uniforms.vertical.currentBuffer());
        }
        uniforms.rotate();
    }

    private RenderTarget swapFor(RenderTarget target) {
        if (this.swap == null) {
            this.swap = new TextureTarget("Emotecraft modal blur", target.width, target.height, GpuFormat.RGBA8_UNORM, null);
        } else if (this.swap.width != target.width || this.swap.height != target.height) {
            this.swap.resize(target.width, target.height);
        }
        return this.swap;
    }

    private static void pass(RenderTarget input, RenderTarget output, GpuBuffer info, GpuBuffer config) {
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Emotecraft modal blur", output.getColorTextureView(), Optional.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(PIPELINE));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", info);
            pass.setUniform("BlurConfig", config);
            pass.setUniform("InSampler", input.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.draw(3, 1, 0, 0);
        }
    }

    /** Of one pass's kernel along its axis: weight 1 up to the whole radius, its fraction one texel further. */
    private static float variance(float radius) {
        int whole = (int) radius;
        float fraction = radius - whole;
        return (whole * (whole + 1) * (2 * whole + 1) / 3.0F + 2.0F * fraction * (whole + 1) * (whole + 1)) / (2.0F * radius + 1.0F);
    }

    private static float radiusOf(float variance, float max) {
        float low = 0.0F;
        float high = max;
        for (int i = 0; i < 16; i++) {
            float middle = (low + high) / 2.0F;
            if (variance(middle) < variance) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return high;
    }

    @Override
    public void close() {
        this.under.close();
        this.over.close();
        if (this.swap != null) this.swap.destroyBuffers();
    }

    private static final class Uniforms implements AutoCloseable {
        private static final int INFO_SIZE = new Std140SizeCalculator().putVec2().putVec2().get();
        private static final int CONFIG_SIZE = new Std140SizeCalculator().putVec2().putFloat().get();
        private static final int USAGE = GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE;

        private final MappableRingBuffer info;
        private final MappableRingBuffer horizontal;
        private final MappableRingBuffer vertical;

        private Uniforms(String name) {
            this.info = new MappableRingBuffer(() -> "Emotecraft " + name + " blur SamplerInfo", USAGE, INFO_SIZE);
            this.horizontal = new MappableRingBuffer(() -> "Emotecraft " + name + " blur horizontal BlurConfig", USAGE, CONFIG_SIZE);
            this.vertical = new MappableRingBuffer(() -> "Emotecraft " + name + " blur vertical BlurConfig", USAGE, CONFIG_SIZE);
        }

        private void write(RenderTarget target, float radius) {
            try (GpuBufferSlice.MappedView view = this.info.currentBuffer().map(false, true)) {
                Std140Builder.intoBuffer(view.data()).putVec2(target.width, target.height).putVec2(target.width, target.height);
            }
            write(this.horizontal, 1.0F, 0.0F, radius);
            write(this.vertical, 0.0F, 1.0F, radius);
        }

        private static void write(MappableRingBuffer config, float x, float y, float radius) {
            try (GpuBufferSlice.MappedView view = config.currentBuffer().map(false, true)) {
                Std140Builder.intoBuffer(view.data()).putVec2(x, y).putFloat(radius);
            }
        }

        private void rotate() {
            this.info.rotate();
            this.horizontal.rotate();
            this.vertical.rotate();
        }

        @Override
        public void close() {
            this.info.close();
            this.horizontal.close();
            this.vertical.close();
        }
    }
}
