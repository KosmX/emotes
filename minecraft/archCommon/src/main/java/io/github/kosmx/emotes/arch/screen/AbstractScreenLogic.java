package io.github.kosmx.emotes.arch.screen;

import net.minecraft.client.gui.GuiGraphics;

import java.nio.file.Path;
import java.util.List;

public abstract class AbstractScreenLogic implements IScreenLogicHelper {
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
}
