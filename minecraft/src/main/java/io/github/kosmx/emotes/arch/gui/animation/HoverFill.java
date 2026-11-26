package io.github.kosmx.emotes.arch.gui.animation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.ARGB;

public final class HoverFill {
    public static final int ROW = ARGB.color(128, 66, 66, 66);

    private final int color;
    private final Animatable hover = new Animatable(0.0F, MotionScheme.FAST_EFFECTS);

    public HoverFill(int color) {
        this.color = color;
    }

    public void extract(GuiGraphicsExtractor graphics, AbstractWidget widget, boolean hovered, float alpha) {
        this.hover.animateTo(hovered ? 1.0F : 0.0F);
        float hover = this.hover.get();
        if (hover > 0.0F) {
            graphics.fill(widget.getX(), widget.getY(), widget.getRight(), widget.getBottom(), ARGB.multiplyAlpha(this.color, hover * alpha));
        }
    }
}
