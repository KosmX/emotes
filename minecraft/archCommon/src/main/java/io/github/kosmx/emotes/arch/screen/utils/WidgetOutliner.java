package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class WidgetOutliner {
    public static void renderOutline(GuiGraphics guiGraphics, LayoutElement element, int color) {
        Identifier headerSeparator = EmotecraftTexture.HEADER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator, element.getX(), element.getY() - 1, 0.0F, 0.0F, element.getWidth(), 2, 32, 2, color);

        drawSeparatorRotated(guiGraphics, headerSeparator, element.getX() - 1, element.getY(), element.getHeight(), -90F, color);
        drawSeparatorRotated(guiGraphics, headerSeparator, element.getX() + element.getWidth() + 1, element.getY(), element.getHeight(), 90F, color);

        Identifier footerSeparator = EmotecraftTexture.FOOTER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator, element.getX(), element.getY() + element.getHeight() - 1, 0.0F, 0.0F, element.getWidth(), 2, 32, 2, color);
    }

    protected static void drawSeparatorRotated(GuiGraphics guiGraphics, Identifier separator, int x, int y, int size, float angle, int color) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y + size / 2.0F);
        guiGraphics.pose().rotate((float) Math.toRadians(angle));
        guiGraphics.pose().translate(-size / 2.0F, 0);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, separator, 0, 0, 0.0F, 0.0F, size, 2, 32, 2, color);
        guiGraphics.pose().popMatrix();
    }
}
