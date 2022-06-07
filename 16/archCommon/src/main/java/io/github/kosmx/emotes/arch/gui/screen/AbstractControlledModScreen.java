package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.IScreenLogicHelper;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface method redirections, default implementations
 */
public abstract class AbstractControlledModScreen extends Screen implements IScreenSlave<PoseStack, Screen> {
    final Screen parent;
    public final AbstractScreenLogic<PoseStack, Screen> master;

    @Override
    public void emotesRenderBackgroundTexture(int vOffset) {
        super.renderDirtBackground(vOffset);
    }

    private int getW() {
        return this.width;
    }

    protected AbstractControlledModScreen(net.minecraft.network.chat.Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.master = newMaster();
    }

    protected abstract AbstractScreenLogic<PoseStack, Screen> newMaster();

    @Override
    public Screen getScreen() {
        return this; //This is a screen after all.
    }

    public interface IScreenHelperImpl extends IScreenLogicHelper<PoseStack>, IDrawableImpl {
        @Override
        default IButton newButton(int x, int y, int width, int height, Text msg, Consumer<IButton> pressAction) {
            return new IButtonImpl(x, y, width, height, ((TextImpl) msg).get(), button -> pressAction.accept((IButton) button));
        }

        @Override
        default ITextInputWidget<PoseStack, TextInputImpl> newTextInputWidget(int x, int y, int width, int height, Text title) {
            return new TextInputImpl(x, y, width, height, (TextImpl) title);
        }

        @Override
        default IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text) {
            return new ConfirmScreenImpl(consumer::accept, ((TextImpl) title).get(), ((TextImpl) text).get());
        }

        @Override
        default void openExternalEmotesDir() {
            Util.getPlatform().openFile(EmoteInstance.instance.getExternalEmoteDir());
        }
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
    public void setInitialFocus(IWidget searchBox) {
        this.setInitialFocus((GuiEventListener) searchBox.get());
    }

    @Override
    public void setFocused(IWidget focused) {
        this.setFocused((GuiEventListener) focused.get());
    }

    @Override
    public void addToChildren(IWidget widget) {
        this.children.add((GuiEventListener) widget.get());
    }

    @Override
    public void addToButtons(IButton button) {
        this.buttons.add((IButtonImpl) button);
    }

    @Override
    public void openParent() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void addButtonsToChildren() {
        this.children.addAll(this.buttons);
    }

    @Override
    public void openScreen(@Nullable IScreen<Screen> screen) {
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
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
