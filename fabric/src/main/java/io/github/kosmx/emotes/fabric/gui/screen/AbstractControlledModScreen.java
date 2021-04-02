package io.github.kosmx.emotes.fabric.gui.screen;

import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import io.github.kosmx.emotes.fabric.executor.types.TextImpl;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.IScreenLogicHelper;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Interface method redirections, default implementations
 */
public abstract class AbstractControlledModScreen extends Screen implements IScreenSlave<MatrixStack, Screen> {
    final Screen parent;
    public final AbstractScreenLogic<MatrixStack, Screen> master;

    @Override
    public void emotesRenderBackgroundTexture(int vOffset) {
        super.renderBackgroundTexture(vOffset);
    }

    private int getW() {
        return this.width;
    }

    protected AbstractControlledModScreen(net.minecraft.text.Text title, Screen parent) {
        super(title);
        this.parent = parent;
        this.master = newMaster();
    }

    protected abstract AbstractScreenLogic<MatrixStack, Screen> newMaster();

    @Override
    public Screen getScreen() {
        return this; //This is a screen after all.
    }

    public interface IScreenHelperImpl extends IScreenLogicHelper<MatrixStack>, IDrawableImpl {
        @Override
        default IButton newButton(int x, int y, int width, int height, Text msg, Consumer<IButton> pressAction) {
            return new IButtonImpl(x, y, width, height, ((TextImpl) msg).get(), button -> pressAction.accept((IButton) button));
        }

        @Override
        default ITextInputWidget<MatrixStack, TextInputImpl> newTextInputWidget(int x, int y, int width, int height, Text title) {
            return new TextInputImpl(x, y, width, height, (TextImpl) title);
        }

        @Override
        default IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text) {
            return new ConfirmScreenImpl(consumer::accept, ((TextImpl) title).get(), ((TextImpl) text).get());
        }
    }
    @Override
    public void openThisScreen() {
        MinecraftClient.getInstance().openScreen(this);
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
        this.buttons.add((IButtonImpl) button);
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
    public void openScreen(@Nullable IScreen<Screen> screen) {
        if(screen != null) {
            MinecraftClient.getInstance().openScreen(screen.getScreen());
        }
        else{
            MinecraftClient.getInstance().openScreen(null);
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        master.emotes_renderScreen(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return master.emotes_isThisPauseScreen();
    }
}
