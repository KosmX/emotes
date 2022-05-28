package io.github.kosmx.emotes.main.screen.widget;

import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stuff fo override
 * void render(MATRIX, int mouseX, int mouseY, float delta)
 * boolean onMouseClicked
 * void isMouseHover
 * @param <MATRIX> Minecraft's MatrixStack
 */
public abstract class AbstractFastChooseWidget<MATRIX, WIDGET> implements IWidgetLogic<MATRIX, WIDGET> {


    public int x;
    public int y;
    protected int size;
    //protected final FastChooseElement[] elements = new FastChooseElement[8];
    protected final ArrayList<FastChooseElement> elements = new ArrayList<>();
    private boolean hovered;
    private final IIdentifier TEXTURE = ((ClientConfig) EmoteInstance.config).dark.get() ? EmoteInstance.instance.getDefaults().newIdentifier("textures/gui/fastchoose_dark_new.png") : EmoteInstance.instance.getDefaults().newIdentifier("textures/gui/fastchoose_light_new.png");

    private AbstractFastChooseWidget(){
        elements.add( new FastChooseElement(0, 22.5f-22.5f));
        elements.add( new FastChooseElement(1, 67.5f-22.5f));
        elements.add( new FastChooseElement(2, 112.5f-22.5f));
        elements.add( new FastChooseElement(3, 157.5f-22.5f));
        elements.add( new FastChooseElement(4, 202.5f-22.5f));
        elements.add( new FastChooseElement(5, 247.5f-22.5f));
        elements.add( new FastChooseElement(6, 292.5f-22.5f));
        elements.add( new FastChooseElement(7, 337.5f-22.5f));
    }

    public AbstractFastChooseWidget(int x, int y, int size){
        this();
        this.x = x;
        this.y = y;
        this.size = size;       //It's a square with same width and height
    }

    public void drawCenteredText(MATRIX matrixStack, Text stringRenderable, float deg){
        drawCenteredText(matrixStack, stringRenderable, (float) (((float) (this.x + this.size / 2)) + size * 0.4 * Math.sin(deg * 0.0174533)), (float) (((float) (this.y + this.size / 2)) + size * 0.4 * Math.cos(deg * 0.0174533)));
    }

    public void drawCenteredText(MATRIX matrices, Text stringRenderable, float x, float y){
        int c = ((ClientConfig)EmoteInstance.config).dark.get() ? 255 : 0; //:D
        textDraw(matrices, stringRenderable, x - (float) textRendererGetWidth(stringRenderable) / 2, y - 2, MathHelper.colorHelper(c, c, c, 1));
    }

    @Nullable
    protected FastChooseElement getActivePart(int mouseX, int mouseY){
        int x = mouseX - this.x - this.size / 2;
        int y = mouseY - this.y - this.size / 2;
        int i = 0;
        double pi = Math.PI;
        float degrees = (float) (Math.abs(((Math.atan2(y , x) - pi) / (2*pi)) * 360 - 270) % 360);
        if (Math.abs(x) <= 10 && Math.abs(y) <= 10){
            return null;
        }
        if (degrees < 22.5) {}
        else if (degrees < 22.5+45){
            i = 1;
        } else if (degrees < 22.5+90){
            i = 2;
        } else if (degrees < 22.5+135){
            i = 3;
        } else if (degrees < 22.5+180){
            i = 4;
        } else if (degrees < 22.5+225){
            i = 5;
        } else if (degrees < 22.5+270){
            i = 6;
        } else if (degrees < 22.5+315){
            i = 7;
        }
        return elements.get(i);
    }

    public void render(MATRIX matrices, int mouseX, int mouseY, float delta){
        checkHovered(mouseX, mouseY);
        renderBindTexture(TEXTURE);
        renderSystemBlendColor(1, 1, 1, 1);
        renderEnableBend();
        renderDefaultBendFunction();
        renderEnableDepthText();
        this.drawTexture(matrices, 0, 0, 0, 0, 2);
        if(this.hovered){
            FastChooseElement part = getActivePart(mouseX, mouseY);
            if(part != null && doHoverPart(part)){
                part.renderHover(matrices);
            }
        }
        for(FastChooseElement f : elements){
            if(f.hasEmote()) f.render(matrices);
        }
    }

    protected abstract boolean doHoverPart(FastChooseElement part);

    /**
     * @param matrices MatrixStack ...
     * @param x        Render x from this pixel
     * @param y        same
     * @param u        texture x
     * @param v        texture y
     * @param s        used texture part size !NOT THE WHOLE TEXTURE IMAGE SIZE!
     */
    private void drawTexture(MATRIX matrices, int x, int y, int u, int v, int s){
        drawableDrawTexture(matrices, this.x + x * this.size / 256, this.y + y * this.size / 256, s * this.size / 2, s * this.size / 2, u, v, s * 128, s * 128, 512, 512);
    }
    private void drawTexture_select(MATRIX matrices, int x, int y, int u, int v, int w, int h){
        drawableDrawTexture(matrices, this.x + x * this.size / 512, this.y + y * this.size / 512, w * this.size / 2, h * this.size / 2, u, v, w * 128, h * 128, 512, 512);
    }

    private void checkHovered(int mouseX, int mouseY){
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX <= this.x + this.size && mouseY <= this.y + this.size;
    }

    public boolean emotes_mouseClicked(double mouseX, double mouseY, int button){
        checkHovered((int) mouseX, (int) mouseY);
        if(this.hovered && this.isValidClickButton(button)){
            FastChooseElement element = this.getActivePart((int) mouseX, (int) mouseY);
            if(element != null){
                return EmotesOnClick(element, button);
            }
        }
        return false;
    }


    public boolean isMouseOver(double mouseX, double mouseY){
        this.checkHovered((int) mouseX, (int) mouseY);
        return this.hovered;
    }

    protected abstract boolean isValidClickButton(int button);

    protected abstract boolean EmotesOnClick(FastChooseElement element, int button);  //What DO I want to do with this element? set or play.

    protected abstract boolean doesShowInvalid();

    protected class FastChooseElement {
        private final float angle;
        private final int id;

        @Nullable

        protected FastChooseElement(int num, float angle){
            this.angle = angle;
            this.id = num;
        }

        public boolean hasEmote(){
            return ((ClientConfig)EmoteInstance.config).fastMenuEmotes[id] != null;
        }

        public void setEmote(@Nullable EmoteHolder emote){
            ((ClientConfig)EmoteInstance.config).fastMenuEmotes[id] = emote == null ? null : emote.getUuid();
        }

        @Nullable
        public EmoteHolder getEmote(){
            UUID uuid = ((ClientConfig)EmoteInstance.config).fastMenuEmotes[id];
            if(uuid != null){
                EmoteHolder emote = EmoteHolder.list.get(uuid);
                if(emote == null && doesShowInvalid()){
                    emote = new EmoteHolder.Empty(uuid);
                }
                return emote;
            }
            else {
                return null;
            }
        }

        public void clearEmote(){
            this.setEmote(null);
        }

        public void render(MATRIX matrices){
            UUID emoteID = ((ClientConfig)EmoteInstance.config).fastMenuEmotes[id] != null ? ((ClientConfig)EmoteInstance.config).fastMenuEmotes[id] : null;
            IIdentifier identifier = emoteID != null && EmoteHolder.list.get(emoteID) != null ? EmoteHolder.list.get(emoteID).getIconIdentifier() : null;
            if(identifier != null && ((ClientConfig)EmoteInstance.config).showIcons.get()){
                int s = size / 10;
                int iconX = (int) (((float) (x + size / 2)) + size * 0.4 * Math.sin(this.angle * 0.0174533)) - s;
                int iconY = (int) (((float) (y + size / 2)) + size * 0.4 * Math.cos(this.angle * 0.0174533)) - s;
                renderBindTexture(identifier);
                drawableDrawTexture(matrices, iconX, iconY, s * 2, s * 2, 0, 0, 256, 256, 256, 256);
            }else{
                if(((ClientConfig)EmoteInstance.config).fastMenuEmotes[id] != null){
                    drawCenteredText(matrices, EmoteHolder.getNonNull(((ClientConfig)EmoteInstance.config).fastMenuEmotes[id]).name, this.angle);
                }else{
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Tried to render non-existing name", true);
                }
            }
        }

        public void renderHover(MATRIX matrices){
            switch (id) {
                case 0:
                    drawTexture_select(matrices, 0, 256, 0, 384, 2, 1);//0
                    break;
                case 1:
                    drawTexture_select(matrices, 256, 256, 384, 384, 1, 1);//1
                    break;
                case 2:
                    drawTexture_select(matrices, 256, 0, 384, 0, 1, 2);//2
                    break;
                case 3:
                    drawTexture_select(matrices, 256, 0, 384, 256, 1, 1);//3
                    break;
                case 4:
                    drawTexture_select(matrices, 0, 0, 0, 256, 2, 1);//4
                    break;
                case 5:
                    drawTexture_select(matrices, 0, 0, 256, 256, 1, 1); //5
                    break;
                case 6:
                    drawTexture_select(matrices, 0, 0, 256, 0, 1, 2);//6
                    break;
                case 7:
                    drawTexture_select(matrices, 0, 256, 256, 384, 1, 1);//7
                    break;
            }
        }
    }
}
