package io.github.kosmx.emotes.arch.gui.dropdown;

import io.github.kosmx.emotes.arch.gui.animation.HoverFill;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class MoreButton extends AbstractButton {
    public static final int SIZE = 12;
    public static final Component DOTS = Component.literal("⋮");

    private final DropdownLayer layer;
    private final AppDropdownMenu menu;
    private final HoverFill hover = new HoverFill(ARGB.white(0.2F));

    public MoreButton(DropdownLayer layer, Consumer<AppDropdownMenu.MenuScope> content) {
        super(0, 0, SIZE, SIZE, DOTS);
        this.layer = layer;
        this.menu = new AppDropdownMenu(layer, this::getRectangle, this::closeMenu, content);
    }

    public void closeMenu() {
        this.menu.setExpanded(false);
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        this.menu.setExpanded(true);
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.isHovered &= !this.layer.isMouseOver(mouseX, mouseY); // The shown menu covers it there, as it covers the list's rows
        this.hover.extract(graphics, this, isHovered(), this.alpha);
        extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
