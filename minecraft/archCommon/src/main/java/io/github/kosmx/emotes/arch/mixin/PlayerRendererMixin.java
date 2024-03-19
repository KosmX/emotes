package io.github.kosmx.emotes.arch.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.MainClientInit;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)//TODO MOD ENABLE ICON
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "RETURN")
    )
    protected void renderNameTag(AbstractClientPlayer entity, Component component, PoseStack stack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {

        //check config enable
        if (!((ClientConfig) EmoteInstance.config).inWorldIcons.get()) {
            return;
        }

        //check server have mod/plugin
        if (MainClientInit.playerHasMode == null) {
            return;
        }

        //check player has mod
        if (MainClientInit.playerHasMode.contains(entity.getUUID())) {
            return;
        }

        Vec3 position = entity.position();

        double distance = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(position);
        if (distance > 4096D) return;

        stack.pushPose();

        if (shouldShowName(entity)) {
            stack.translate(0D, 0.3D, 0D);

            Objective belowNameObjective = entity.getScoreboard().getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (belowNameObjective != null && distance < 100D) {
                stack.translate(0D, 0.3D, 0D);
            }
        }
        //TODO NEED ADD PLASMOVOICE CHECKER & translate if plasmo have self Icon OR use other place for Emote Icon
        stack.translate(0.0D, entity.getNameTagOffsetY(), 0.0D);
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        stack.scale(-0.03F, -0.03F, 0.03F);
        stack.translate(-5D, -1D, 0D);

        RenderSystem._setShaderTexture(0, MainClientInit.TEXTURE_NO_MOD);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        // TRANSLUCENT_TRANSPARENCY
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 771);
        // LIGHT
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        if (entity.isDescending()) {
            emotecraft$vertices(stack, 40, light, false);
        } else {
            emotecraft$vertices(stack, 255, light, false);
            emotecraft$vertices(stack, 40, light, true);
        }

        stack.popPose();

        // TRANSLUCENT_TRANSPARENCY
        RenderSystem.disableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.depthMask(true);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
    }

    @Unique
    private void emotecraft$vertices(PoseStack stack,
                                     int alpha,
                                     int light,
                                     boolean seeThrough) {
        if (seeThrough) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }

        if (seeThrough) {
            RenderSystem.setShader(GameRenderer::getRendertypeTextSeeThroughShader);
        } else {
            RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
        }

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
        );
        Matrix4f model = stack.last().pose();
        Matrix3f norm = stack.last().normal();

        emotecraft$vertex(model, norm, buffer, 0F, 10F, 0F, 0F, 1F, alpha, light);
        emotecraft$vertex(model, norm, buffer, 10F, 10F, 0F, 1F, 1F, alpha, light);
        emotecraft$vertex(model, norm, buffer, 10F, 0F, 0F, 1F, 0F, alpha, light);
        emotecraft$vertex(model, norm, buffer, 0F, 0F, 0F, 0F, 0F, alpha, light);

        Tesselator.getInstance().end();
    }

    @Unique
    private void emotecraft$vertex(Matrix4f model,
                                   Matrix3f norm,
                                   BufferBuilder buffer,
                                   float x, float y, float z, float u, float v, int alpha, int light) {
        buffer.vertex(model, x, y, z);
        buffer.color(255, 255, 255, alpha);
        buffer.uv(u, v);
        buffer.overlayCoords(0, 10);
        buffer.uv2(light & '\uffff', light >> 16 & '\uffff');
        buffer.normal(norm,0F, 0F, -1F);


        buffer.endVertex();
    }
}
