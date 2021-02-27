package com.kosmx.emotes.fabric.gui.screen;

import com.kosmx.emotes.executor.dataTypes.IIdentifier;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.fabric.executor.types.IdentifierImpl;
import com.kosmx.emotes.fabric.executor.types.TextImpl;
import com.kosmx.emotes.main.screen.IRenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public interface IDrawableImpl extends IRenderHelper<MatrixStack> {
    @Override
    default void renderSystemBlendColor(float r, float g, float b, float a){
        RenderSystem.blendColor(r, g, b, a);
    }

    @Override
    default void drawableHelperFill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color){
        DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
    }

    @Override
    default void textDrawWithShadow(MatrixStack matrices, Text text, float x, float y, int color){
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, ((TextImpl)text).get(), x, y, color);
    }

    @Override
    default void textDraw(MatrixStack matrices, Text text, float x, float y, int color){
        MinecraftClient.getInstance().textRenderer.draw(matrices, ((TextImpl)text).get(), x, y, color);
    }

    @Override
    default int textRendererGetWidth(Text text){
        return MinecraftClient.getInstance().textRenderer.getWidth(((TextImpl)text).get());
    }

    @Override
    default void renderBindTexture(IIdentifier texture){
        MinecraftClient.getInstance().getTextureManager().bindTexture(((IdentifierImpl)texture).get());
    }

    @Override
    default void renderEnableBend(){
        RenderSystem.enableBlend();
    }

    @Override
    default void renderDisableBend(){
        RenderSystem.disableBlend();
    }

    @Override
    default void renderDefaultBendFunction(){
        RenderSystem.defaultBlendFunc();
    }

    @Override
    default void renderEnableDepthText(){
        RenderSystem.enableDepthTest();
    }

    @Override
    default void drawableDrawTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight){
        DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    @Override
    default void drawCenteredText(MatrixStack matrices, Text text, int centerX, int y, int color){
        DrawableHelper.drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, ((TextImpl)text).get(), centerX, y, color);
    }
}
