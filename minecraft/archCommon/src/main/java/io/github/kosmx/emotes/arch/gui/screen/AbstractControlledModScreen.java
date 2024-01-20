package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface method redirections, default implementations
 */
public abstract class AbstractControlledModScreen extends Screen implements IScreenSlave {
    final Screen parent;
    public final AbstractScreenLogic master;

    @Override
    public void emotesRenderBackgroundTexture(GuiGraphics poseStack) {
        super.renderDirtBackground(poseStack);
    }

    private int getW() {
        return this.width;
    }

    protected AbstractControlledModScreen(net.minecraft.network.chat.Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.master = newMaster();
    }

    protected abstract AbstractScreenLogic newMaster();

    @Override
    public Screen getScreen() {
        return this; //This is a screen after all.
    }

    public interface IScreenHelperImpl {
    }
    @Override
    public void openThisScreen() {
        Minecraft.getInstance().setScreen(this);
    }

    @Override
    public int getWidth() {
        return getW();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setInitialFocus(GuiEventListener searchBox) {
        setInitialFocus(searchBox);
    }

    @Override
    public void addToChildren(GuiEventListener widget) {

    }

    @Override
    public void addToButtons(Button button) {
        this.addRenderableWidget(button);
    }

    @Override
    public void openParent() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void addButtonsToChildren() {

    }

    @Override
    public void openScreen(IScreenSlave screen) {
        if(screen != null) {
            Minecraft.getInstance().setScreen(screen.getScreen());
        }
        else{
            Minecraft.getInstance().setScreen(null);
        }
    }
    @Override
    public void init() {
        super.init();
        master.emotes_initScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return master.emotes_onKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return master.emotes_onMouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        master.emotes_onRemove();
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
        master.emotes_tickScreen();
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        master.emotes_renderScreen(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return master.emotes_isThisPauseScreen();
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        master.emotes_filesDropped(list);
        super.onFilesDrop(list);
    }
}
