package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

/**
 * Merged version of {@link net.minecraft.client.gui.components.StateSwitchingButton} and {@link AbstractButton} into one class for convenient usage.
 */
public class PageButton extends AbstractButton {
    public static final int PAGE_BUTTON_WIDTH = 12;
    public static final int PAGE_BUTTON_HEIGHT = 17;

    protected final WidgetSprites sprites;
    protected final boolean drawBackground;
    protected final Consumer<PageButton> onPress;

    public PageButton(WidgetSprites sprites, boolean background, Consumer<PageButton> onPress) {
        this(PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT, sprites, background, onPress);
    }

    public PageButton(int width, int height, WidgetSprites sprites, boolean background, Consumer<PageButton> onPress) {
        this(0, 0, width, height, sprites, background, onPress);
    }

    public PageButton(int x, int y, int width, int height, WidgetSprites sprites, boolean background, Consumer<PageButton> onPress) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.sprites = sprites;
        this.drawBackground = background;
        this.onPress = onPress;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.drawBackground) super.renderDefaultSprite(guiGraphics);

        int width = this.drawBackground ? PAGE_BUTTON_WIDTH : getWidth();
        int height = this.drawBackground ? PAGE_BUTTON_HEIGHT : getHeight();

        int x = getX();
        int y = getY();

        if (this.drawBackground) {
            x += (getWidth() - width) / 2;
            y += (getHeight() - height) / 2;
        }

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(this.active, this.active && isHoveredOrFocused()), x, y,
                width, height, ARGB.white(this.alpha)
        );
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.onPress.accept(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
