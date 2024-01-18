package io.github.kosmx.emotes.arch.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IRenderHelper {
    void renderSystemBlendColor(float r, float g, float b, float a);
    void drawableHelperFill(GuiGraphics matrices, int x1, int y1, int x2, int y2, int color);
    void textDrawWithShadow(GuiGraphics matrices, Component text, float x, float y, int color);
    void textDraw(GuiGraphics matrices, Component text, float x, float y, int color);
    int textRendererGetWidth(Component text);
    void renderBindTexture(ResourceLocation texture);
    void renderEnableBend();
    void renderDisableBend();
    void renderDefaultBendFunction();
    void renderEnableDepthText();
    void drawableDrawTexture(GuiGraphics matrices, ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight);
    void drawCenteredText(GuiGraphics matrices, Component text, int centerX, int y, int color);
}
