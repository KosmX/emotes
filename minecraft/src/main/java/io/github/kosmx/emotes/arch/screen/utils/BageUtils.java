package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Some of the code was copied:
 * - modmenu: badges because they are pretty
 * - minecraft: calculate text color and shadows
 */
public class BageUtils {
    private static final int SPACE = 3;

    public static int drawBadges(GuiGraphics guiGraphics, Font font, List<Component> text, int x, int y, int width, boolean right) {
        int bageX = right ? SPACE : x;
        for (Component bage : text) {
            int tagWidth = font.width(bage) + 6;
            if (bageX + tagWidth + SPACE < width) {
                BageUtils.drawBadge(guiGraphics, font, bage, right ? (x - tagWidth) - bageX : bageX, y, tagWidth, -1);
                bageX += tagWidth + SPACE;
            }
        }
        return bageX;
    }

    public static void drawBadge(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        BageUtils.drawBadge(guiGraphics, font, text, x, y, font.width(text) + 6, color);
    }

    public static void drawBadge(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int tagWidth, int color) {
        TextColor textColor = text.getStyle().getColor();
        int outlineColor = BageUtils.getTextColor(textColor, color);
        int fillColor = BageUtils.getShadowColor(text.getStyle(), outlineColor);

        guiGraphics.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
        guiGraphics.fill(x, y, x + 1, y + font.lineHeight, outlineColor);
        guiGraphics.fill(x + 1,
                y + 1 + font.lineHeight - 1,
                x + tagWidth,
                y + font.lineHeight + 1,
                outlineColor
        );
        guiGraphics.fill(x + tagWidth, y, x + tagWidth + 1, y + font.lineHeight, outlineColor);
        guiGraphics.fill(x + 1, y, x + tagWidth, y + font.lineHeight, fillColor);
        guiGraphics.drawString(font,
                text,
                (int) (x + 1 + (tagWidth - font.width(text)) / (float) 2),
                y + 1,
                color,
                false
        );
    }

    private static int getTextColor(@Nullable TextColor textColor, int color) {
        if (textColor != null) {
            int i = ARGB.alpha(color);
            int j = textColor.getValue();
            return ARGB.color(i, j);
        } else {
            return color;
        }
    }

    private static int getShadowColor(Style style, int textColor) {
        Integer integer = style.getShadowColor();
        if (integer != null) {
            float f = ARGB.alphaFloat(textColor);
            float g = ARGB.alphaFloat(integer);
            return f != 1.0F ? ARGB.color(ARGB.as8BitChannel(f * g), integer) : integer;
        } else {
            return ARGB.scaleRGB(textColor, 0.25F);
        }
    }
}
