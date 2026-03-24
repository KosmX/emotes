package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class WidgetOutliner {
    public static void extractOutline(GuiGraphicsExtractor graphics, LayoutElement element, int color) {
        Identifier headerSeparator = EmotecraftTexture.HEADER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator, element.getX(), element.getY() - 1, 0.0F, 0.0F, element.getWidth(), 2, 32, 2, color);

        extractSeparatorRotated(graphics, headerSeparator, element.getX() - 1, element.getY(), element.getHeight(), -90F, color);
        extractSeparatorRotated(graphics, headerSeparator, element.getX() + element.getWidth() + 1, element.getY(), element.getHeight(), 90F, color);

        Identifier footerSeparator = EmotecraftTexture.FOOTER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator, element.getX(), element.getY() + element.getHeight() - 1, 0.0F, 0.0F, element.getWidth(), 2, 32, 2, color);
    }

    protected static void extractSeparatorRotated(GuiGraphicsExtractor graphics, Identifier separator, int x, int y, int size, float angle, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y + size / 2.0F);
        graphics.pose().rotate((float) Math.toRadians(angle));
        graphics.pose().translate(-size / 2.0F, 0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, separator, 0, 0, 0.0F, 0.0F, size, 2, 32, 2, color);
        graphics.pose().popMatrix();
    }
}
