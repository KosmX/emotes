package io.github.kosmx.emotes.main.screen.widget;

import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.main.config.ClientConfig;

public abstract class AbstractFastChooseWidget<MATRIX, WIDGET> implements IWidgetLogic<MATRIX, WIDGET> {
    private IChooseWheel<MATRIX> wheel;


    public final int x;
    public final int y;
    public final int size;


    protected AbstractFastChooseWidget(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.wheel = IChooseWheel.getWheel(this);
    }

    protected void bind(IChooseWheel<MATRIX> wheel) {
        this.wheel = wheel;
    }


    public void render(MATRIX matrices, int mouseX, int mouseY, float delta) {
        this.wheel.render(matrices, mouseX, mouseY, delta);
    }


    public void drawCenteredText(MATRIX matrixStack, Text stringRenderable, float deg){
        drawCenteredText(matrixStack, stringRenderable, (float) (((float) (this.x + this.size / 2)) + size * 0.4 * Math.sin(deg * 0.0174533)), (float) (((float) (this.y + this.size / 2)) + size * 0.4 * Math.cos(deg * 0.0174533)));
    }

    public void drawCenteredText(MATRIX matrices, Text stringRenderable, float x, float y){
        int c = ((ClientConfig) EmoteInstance.config).dark.get() ? 255 : 0; //:D
        textDraw(matrices, stringRenderable, x - (float) textRendererGetWidth(stringRenderable) / 2, y - 2, MathHelper.colorHelper(c, c, c, 1));
    }


    protected abstract boolean doHoverPart(IChooseWheel.IChooseElement part);

    protected abstract boolean isValidClickButton(int button);

    protected abstract boolean EmotesOnClick(IChooseWheel.IChooseElement element, int button);  //What DO I want to do with this element? set or play.

    protected abstract boolean doesShowInvalid();

    public boolean emotes_mouseClicked(double mouseX, double mouseY, int button) {
        return this.wheel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean emotes_mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.wheel.mouseScrolled(mouseX, mouseY, amount);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.wheel.isMouseOver(mouseX, mouseY);
    }
}
