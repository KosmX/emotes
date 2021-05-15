package io.github.kosmx.emotes.main.screen;

import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class AbstractScreenLogic<MATRIX, SCREEN> implements IScreenLogicHelper<MATRIX> {
    protected final IScreenSlave<MATRIX, SCREEN> screen;

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
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float tickDelta){

    }
    public boolean emotes_isThisPauseScreen(){
        return true;
    }

    public void emotes_filesDropped(List<Path> files){}
}
