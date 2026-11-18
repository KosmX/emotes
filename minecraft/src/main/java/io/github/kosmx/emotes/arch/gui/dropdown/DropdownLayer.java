package io.github.kosmx.emotes.arch.gui.dropdown;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/** The dropdown menu shown on a screen, which draws it over everything and gives it input first, as {@code ChatScreen} does its command suggestions. */
public final class DropdownLayer {
    private @Nullable AppDropdownMenu shown;

    void show(AppDropdownMenu menu) {
        this.shown = menu;
    }

    boolean isShowing(AppDropdownMenu menu) {
        return this.shown == menu && !menu.isClosed();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.shown != null && this.shown.isExpanded() && this.shown.isMouseOver(mouseX, mouseY);
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.shown == null || this.shown.isClosed()) return;
        graphics.nextStratum();
        this.shown.extractRenderState(graphics, mouseX, mouseY, a);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.shown != null && this.shown.mouseClicked(event, doubleClick);
    }

    public boolean keyPressed(KeyEvent event) {
        return this.shown != null && this.shown.keyPressed(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.shown != null && this.shown.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean updateNarration(NarrationElementOutput output) {
        if (this.shown == null || !this.shown.isExpanded()) return false;
        this.shown.updateNarration(output);
        return true;
    }
}
