package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.utils.TransparentButton;
import io.github.kosmx.emotes.arch.screen.widget.preview.PlayerChooseElement;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFastChooseWidget extends AbstractWidget implements ContainerEventHandler {
    protected final List<AbstractWidget> elements = new ArrayList<>();
    public final FastChooseController controller;

    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    protected final TransparentButton forwardButton = new TransparentButton(15, 15, McUtils.FORWARD, this::onForwardButton);
    protected final TransparentButton backButton = new TransparentButton(15, 15, McUtils.BACK, this::onBackButton);

    private int currentPage;

    protected AbstractFastChooseWidget(FastChooseController controller, int x, int y, int size, Component message) {
        super(x, y, size, size, message);
        this.controller = controller;

        this.elements.add(this.forwardButton);
        this.elements.add(this.backButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;

        this.forwardButton.setPosition((centerX - this.forwardButton.getWidth() / 2) + globalPadding(), centerY - this.forwardButton.getHeight() / 2);
        this.backButton.setPosition((centerX - this.forwardButton.getWidth() / 2) - globalPadding(), centerY - this.forwardButton.getHeight() / 2);

        Component text = Component.literal(String.valueOf(this.currentPage + 1));
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text, centerX - (textWidth / 2), centerY - (font.lineHeight / 3), -1);

        for (Renderable renderable : this.elements) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public int globalPadding() {
        return getWidth() / 8;
    }

    public abstract void tick();

    public void removed() {
        for (AbstractWidget widget : this.elements) {
            if (widget instanceof IChooseElement element) element.removed();
        }
    }

    @Override
    public @NotNull List<AbstractWidget> children() {
        return this.elements;
    }

    @Override
    public boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public void setDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }

    @Override
    public @Nullable GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null) this.focused.setFocused(false);
        if (focused != null) focused.setFocused(true);
        this.focused = focused;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // no-op
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    protected void onForwardButton(TransparentButton button) {
        if (this.currentPage < PlatformTools.getConfig().fastMenuEmotes.length - 1) {
            this.currentPage += 1;
        } else {
            this.currentPage = 0;
        }
    }

    protected void onBackButton(TransparentButton button) {
        if (this.currentPage > 0) {
            this.currentPage -= 1;
        } else {
            this.currentPage = PlatformTools.getConfig().fastMenuEmotes.length - 1;
        }
    }

    private double scrollAccumulator = 0.0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        boolean ret = ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        this.scrollAccumulator += scrollX;

        int maxPages = PlatformTools.getConfig().fastMenuEmotes.length;
        while (this.scrollAccumulator >= 2.0) {
            this.scrollAccumulator -= 2.0;
            currentPage = (currentPage + 1) % maxPages;
            ret = true;
        }
        while (this.scrollAccumulator <= -2.0) {
            this.scrollAccumulator += 2.0;
            currentPage = (currentPage - 1 + maxPages) % maxPages;
            ret = true;
        }
        return ret;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        ContainerEventHandler.super.setFocused(focused);
    }
}
