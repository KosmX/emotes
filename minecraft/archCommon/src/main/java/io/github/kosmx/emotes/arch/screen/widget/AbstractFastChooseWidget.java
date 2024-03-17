package io.github.kosmx.emotes.arch.screen.widget;

import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class AbstractFastChooseWidget implements IWidgetLogic {
    private IChooseWheel wheel;


    public final int x;
    public final int y;
    public final int size;


    protected AbstractFastChooseWidget(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.wheel = IChooseWheel.getWheel(this);
    }

    protected void bind(IChooseWheel wheel) {
        this.wheel = wheel;
    }


    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        this.wheel.render(matrices, mouseX, mouseY, delta);
    }


    public void drawCenteredText(GuiGraphics matrixStack, Component stringRenderable, float deg, double offset){
        drawCenteredText(matrixStack, stringRenderable, (float) (((float) (this.x + this.size / 2)) + size * 0.4 * Math.sin(deg * 0.0174533)), (float) (((float) (this.y + this.size / offset)) + size * 0.4 * Math.cos(deg * 0.0174533)));
    }

    public void drawCenteredText(GuiGraphics matrices, Component stringRenderable, float x, float y){
        int c = 255;//TODO ONLY WALTER WHITE //:D
        float x1 = x - (float) Minecraft.getInstance().font.width(stringRenderable) / 2;
        matrices.drawString(Minecraft.getInstance().font, stringRenderable, (int) x1, (int) (y - 2), MathHelper.colorHelper(c, c, c, 1));
    }


    protected abstract boolean doHoverPart(IChooseWheel.IChooseElement part);

    protected abstract boolean isValidClickButton(int button);

    protected abstract boolean onClick(IChooseWheel.IChooseElement element, int button);  //What DO I want to do with this element? set or play.

    protected abstract boolean doesShowInvalid();

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.wheel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.wheel.mouseScrolled(mouseX, mouseY, verticalAmount);
    }


    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.wheel.isMouseOver(mouseX, mouseY);
    }
}
