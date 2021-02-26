package com.kosmx.emotes.main.screen;

public abstract class AbstractScreenLogic<MATRIX, SCREEN> implements IScreenLogicHelper<MATRIX> {
    protected final IScreenSlave<MATRIX, SCREEN> screen;

    protected AbstractScreenLogic(IScreenSlave screen) {
        this.screen = screen;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int mod){
        return false;
    }
    public boolean onMouseClicked(double mouseX, double mouseY, int button){
        return false;
    }
    public void onRemove(){

    }
    public void tickScreen(){

    }
    public abstract void initScreen();
    public void renderScreen(MATRIX matrices, int mouseX, int mouseY, float tickDelta){

    }
    public boolean isThisPauseScreen(){
        return true;
    }

}
