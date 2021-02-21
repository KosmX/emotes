package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.executor.dataTypes.IIdentifier;
import com.kosmx.emotes.executor.dataTypes.Text;

public abstract class AbstractScreenElementLogic<MATRIX> {
    protected abstract void renderSystemBlendColor(float r, float g, float b, float a);
    protected abstract void drawableHelperFill(MATRIX matrices, int x1, int y1, int x2, int y2, int color);
    protected abstract void textDrawWithShadow(MATRIX matrices, Text text, float x, float y, int color);
    protected abstract void renderBindTexture(IIdentifier texture);
    protected abstract void renderEnableBend();
    protected abstract void renderDisableBend();
    protected abstract void drawableDrawTexture(MATRIX matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight);

}
