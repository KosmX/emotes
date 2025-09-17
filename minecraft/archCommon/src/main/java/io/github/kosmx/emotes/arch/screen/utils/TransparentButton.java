package io.github.kosmx.emotes.arch.screen.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

public class TransparentButton extends AbstractButton {
    protected final Consumer<TransparentButton> onPress;

    public TransparentButton(int width, int height, Component message, Consumer<TransparentButton> onPress) {
        this(0, 0, width, height, message, onPress);
    }

    public TransparentButton(int x, int y, int width, int height, Component message, Consumer<TransparentButton> onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.onPress.accept(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = ARGB.color(this.alpha, this.active ? -1 : -6250336);
        this.renderString(guiGraphics, Minecraft.getInstance().font, i);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
