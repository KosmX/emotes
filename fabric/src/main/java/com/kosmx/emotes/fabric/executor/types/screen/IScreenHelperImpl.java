package com.kosmx.emotes.fabric.executor.types.screen;

import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import com.kosmx.emotes.fabric.executor.types.TextImpl;
import com.kosmx.emotes.main.screen.IScreenLogic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface method redirections, default implementations
 */
public class IScreenHelperImpl extends Screen implements IScreenLogic<MatrixStack>, IDrawableImpl {
    final Screen parent;

    protected IScreenHelperImpl(net.minecraft.text.Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    public IButton newButton(int x, int y, int width, int heitht, Text msg, Consumer<IButton> pressAction) {
        return new IButtonImpl(x, y, width, height, ((TextImpl) msg).get(), button -> pressAction.accept((IButton) button));
    }

    @Override
    public ITextInputWidget<MatrixStack, TextInputImpl> newTextInputWidget(int x, int y, int width, int height, Text title) {
        return new TextInputImpl(x, y, width, height, (TextImpl) title);
    }

    @Override
    public IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text) {
        throw new IllegalArgumentException("Config screen is not coded yet...");
        //return null; //TODO
    }

    @Override
    public void openThisScreen() {
        MinecraftClient.getInstance().openScreen(this);
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
    public void setInitialFocus(IWidget searchBox) {
        this.setInitialFocus((Element) searchBox.get());
    }

    @Override
    public void setFocused(IWidget focused) {
        this.setFocused((Element) focused.get());
    }

    @Override
    public void addToChildren(IWidget widget) {
        this.children.add((Element) widget.get());
    }

    @Override
    public void addToButtons(IButton button) {
        this.buttons.add((IButtonImpl)button);
    }

    @Override
    public void openParent() {
        this.client.openScreen(this.parent);
    }

    @Override
    public void addButtonsToChildren() {
        this.children.addAll(this.buttons);
    }

    @Override
    public void openScreen(@Nullable IScreenLogic<MatrixStack> screen) {
        MinecraftClient.getInstance().openScreen((IScreenHelperImpl)screen);
    }

    @Override
    protected void init() {
        super.init();
        this.initScreen();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return onKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onMouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        this.onRemove();
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
        this.tickScreen();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.renderScreen(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return this.isThisPauseScreen();
    }
}
