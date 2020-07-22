package com.kosmx.emotecraft.screen.widget;

import jdk.internal.jline.internal.Nullable;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractFastChooseWidget extends DrawableHelper implements Drawable, Element {
    public int x;
    public int y;
    protected int size;
    protected final FastChooseElement[] elements = new FastChooseElement[8];


    public AbstractFastChooseWidget(int x, int y, int size){
        this.x = x;
        this.y = y;
        this.size = size;
    }

    @Nullable
    protected FastChooseElement getActivePart(int mouseX, int mouseY){
        int x = mouseX - this.x - this.size/2;
        int y = mouseY - this.y - this.size/2;
        int i = 0;
        if(x == 0){
            return null;
        }
        else if(x < 0){
            i += 4;
        }

        if(y == 0){
            return null;
        }
        else if (y < 0){
            i += 2;
        }

        if (Math.abs(x) == Math.abs(y)){
            return null;
        }
        else if(Math.abs(x) > Math.abs(y)){
            i++;
        }
        return elements[i];
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

    }

    protected static class FastChooseElement{

        protected FastChooseElement(int x){

        }

    }
}
