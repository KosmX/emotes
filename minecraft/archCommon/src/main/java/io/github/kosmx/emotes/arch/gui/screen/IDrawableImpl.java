package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kosmx.emotes.arch.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.arch.screen.IRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public interface IDrawableImpl extends IRenderHelper<GuiGraphics> {
    @Override
    default void renderSystemBlendColor(float r, float g, float b, float a){
        RenderSystem.setShaderColor(r, g, b, a);
    }

    @Override
    default void drawableHelperFill(GuiGraphics matrices, int x1, int y1, int x2, int y2, int color){
        matrices.fill(x1, y1, x2, y2, color);
    }

    @Override
    default void textDrawWithShadow(GuiGraphics matrices, Text text, float x, float y, int color){
        matrices.drawString(Minecraft.getInstance().font, ((TextImpl)text).get(), (int) x, (int) y, color);
    }

    @Override
    default void textDraw(GuiGraphics matrices, Text text, float x, float y, int color){
        matrices.drawString(Minecraft.getInstance().font, ((TextImpl)text).get(), (int) x, (int) y, color);
    }

    @Override
    default int textRendererGetWidth(Text text){
        return Minecraft.getInstance().font.width(((TextImpl)text).get());
    }


    @Override
    default void renderBindTexture(IIdentifier texture){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ((IdentifierImpl)texture).get());
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
    default void drawableDrawTexture(GuiGraphics graphics, IIdentifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight){
        graphics.blit(((IdentifierImpl)texture).get(), x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    @Override
    default void drawCenteredText(GuiGraphics graphics, Text text, int centerX, int y, int color){
        graphics.drawCenteredString(Minecraft.getInstance().font, ((TextImpl)text).get(), centerX, y, color);
    }
}
