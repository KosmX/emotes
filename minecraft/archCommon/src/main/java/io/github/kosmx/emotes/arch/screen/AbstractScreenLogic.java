package io.github.kosmx.emotes.arch.screen;

import io.github.kosmx.emotes.arch.gui.screen.IButtonImpl;
import io.github.kosmx.emotes.arch.gui.screen.TextInputImpl;
import io.github.kosmx.emotes.executor.EmoteInstance;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractScreenLogic {
    protected final IScreenSlave screen;

    protected AbstractScreenLogic(IScreenSlave screen) {
        this.screen = screen;
    }

    public boolean emotes_onKeyPressed(int keyCode, int scanCode, int mod){
        return false;
    }
    public boolean emotes_onMouseClicked(double mouseX, double mouseY, int button){
        return false;
    }
    public void emotes_onRemove(){

    }
    public void emotes_tickScreen(){

    }
    public abstract void emotes_initScreen();
    public void emotes_renderScreen(GuiGraphics matrices, int mouseX, int mouseY, float tickDelta){

    }
    public boolean emotes_isThisPauseScreen(){
        return true;
    }

    public void emotes_filesDropped(List<Path> files){}
    public IButtonImpl newButton(int x, int y, int width, int height, Component msg, Consumer<IButtonImpl> pressAction) {
        return new IButtonImpl(x, y, width, height, msg, button -> pressAction.accept((IButtonImpl) button));
    }

    public TextInputImpl newTextInputWidget(int x, int y, int width, int height, Component title) {
        return new TextInputImpl(x, y, width, height, title);
    }

    public ConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Component title, Component text) {
        return new ConfirmScreen(consumer::accept, title, text);
    }
    public void openExternalEmotesDir() {
        Util.getPlatform().openFile(EmoteInstance.instance.getExternalEmoteDir());
    }
}
