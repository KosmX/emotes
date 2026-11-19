package io.github.kosmx.emotes.arch.gui.widgets;

import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/** Text on the outlined panel, laid out as vanilla's {@link FocusableTextWidget} lays out its own. */
public final class GuideWidget extends MultiLineTextWidget {
    private final int preferredWidth;

    public GuideWidget(Component message, Font font, int width) {
        super(message, font);
        this.preferredWidth = width;
        setWidth(width);
    }

    public void centerIn(int left, int top, int right, int bottom) {
        int width = right - left - 2 * Button.DEFAULT_SPACING;
        if (width > 2 * FocusableTextWidget.DEFAULT_PADDING) {
            narrowTo(width);
            FrameLayout.centerInRectangle(this, left, top, right - left, bottom - top);
        }
        this.visible = width > 2 * FocusableTextWidget.DEFAULT_PADDING && getHeight() <= bottom - top;
    }

    public void narrowTo(int width) {
        setWidth(Math.min(this.preferredWidth, width));
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        setMaxWidth(width - 2 * FocusableTextWidget.DEFAULT_PADDING);
        setHeight(super.getHeight() + 2 * FocusableTextWidget.DEFAULT_PADDING);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    protected int getTextX() {
        return getX() + FocusableTextWidget.DEFAULT_PADDING;
    }

    @Override
    protected int getTextY() {
        return getY() + FocusableTextWidget.DEFAULT_PADDING;
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        WidgetOutliner.extractPanel(graphics, getX(), getY(), getWidth(), getHeight(), this.alpha);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }
}
