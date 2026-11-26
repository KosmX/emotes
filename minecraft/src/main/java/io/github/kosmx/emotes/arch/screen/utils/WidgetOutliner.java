package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class WidgetOutliner {
    public static final int REACH = 1; // The separators cover this much on both sides of an edge
    private static final float PANEL_OPACITY = 0.71F;

    /** An outlined panel on vanilla's text background. */
    public static void extractPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, float alpha) {
        int background = Minecraft.getInstance().options.getBackgroundColor(PANEL_OPACITY);
        graphics.fill(x + REACH, y + REACH, x + width - REACH, y + height - REACH, ARGB.multiplyAlpha(background, alpha));
        extractOutline(graphics, x, y, width, height, ARGB.white(alpha));
    }

    public static void extractOutline(GuiGraphicsExtractor graphics, LayoutElement element, int color) {
        extractOutline(graphics, element.getX(), element.getY(), element.getWidth(), element.getHeight(), color);
    }

    public static void extractOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        Identifier headerSeparator = EmotecraftTexture.HEADER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, headerSeparator, x, y - 1, 0.0F, 0.0F, width, 2, 32, 2, color);

        extractSeparatorRotated(graphics, headerSeparator, x - 1, y, height, -90F, color);
        extractSeparatorRotated(graphics, headerSeparator, x + width + 1, y, height, 90F, color);

        Identifier footerSeparator = EmotecraftTexture.FOOTER_SEPARATOR.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, footerSeparator, x, y + height - 1, 0.0F, 0.0F, width, 2, 32, 2, color);
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
