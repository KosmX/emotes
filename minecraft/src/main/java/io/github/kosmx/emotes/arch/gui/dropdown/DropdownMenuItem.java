package io.github.kosmx.emotes.arch.gui.dropdown;

import io.github.kosmx.emotes.arch.gui.animation.HoverFill;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

final class DropdownMenuItem extends AbstractButton {
    private static final Identifier CHECKMARK_SPRITE = Identifier.withDefaultNamespace("icon/checkmark");
    private static final int CHECKMARK_WIDTH = 9; // Material's 20 dp icon
    private static final int CHECKMARK_HEIGHT = 8;
    private static final int PADDING = 6;
    private static final int ICON_GAP = 4;
    private static final int SELECTED_COLOR = ARGB.white(0.12F);

    private final boolean selected;
    private final boolean checkColumn; // In a group with a choice every label starts after the check, so they line up
    private final Runnable onClick;
    private final HoverFill hover = new HoverFill(HoverFill.ROW);

    DropdownMenuItem(Component text, boolean selected, boolean checkColumn, Runnable onClick) {
        super(0, 0, 0, AppDropdownMenu.ITEM_HEIGHT, text);
        this.selected = selected;
        this.checkColumn = checkColumn;
        this.onClick = onClick;
    }

    int contentWidth(Font font) {
        return 2 * PADDING + (this.checkColumn ? CHECKMARK_WIDTH + ICON_GAP : 0) + font.width(getMessage());
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        this.onClick.run();
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.selected) {
            graphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.multiplyAlpha(SELECTED_COLOR, this.alpha));
        }
        this.hover.extract(graphics, this, isHoveredOrFocused(), this.alpha);

        int left = getX() + PADDING;
        if (this.selected) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHECKMARK_SPRITE, left, getY() + (getHeight() - CHECKMARK_HEIGHT) / 2, CHECKMARK_WIDTH, CHECKMARK_HEIGHT, this.alpha);
        }
        if (this.checkColumn) {
            left += CHECKMARK_WIDTH + ICON_GAP;
        }
        graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrolling(getMessage(), left, left, getRight() - PADDING, getY(), getBottom());
    }

    @Override
    protected @NonNull MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.selected ? Component.translatable("narrator.select", getMessage()) : getMessage());
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false; // Focused by a click, a row would stay highlighted once the pointer moves on to another
    }
}
