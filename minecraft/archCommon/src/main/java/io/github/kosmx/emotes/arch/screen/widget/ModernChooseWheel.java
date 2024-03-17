package io.github.kosmx.emotes.arch.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import static io.github.kosmx.emotes.common.SerializableConfig.Icon.*;

/**
 * Stuff fo override
 * void render(MATRIX, int mouseX, int mouseY, float delta)
 * boolean onMouseClicked
 * void isMouseHover
 */
public class ModernChooseWheel implements IChooseWheel {

    public static int fastMenuPage = 0;

    //protected final FastChooseElement[] elements = new FastChooseElement[8];
    protected final ArrayList<FastChooseElement> elements = new ArrayList<>();
    private boolean hovered;
    private final ResourceLocation TEXTURE = ((ClientConfig) EmoteInstance.config).dark.get() ? PlatformTools.newIdentifier("textures/gui/fastchoose_dark_new.png") : PlatformTools.newIdentifier("textures/gui/fastchoose_light_new.png");

    private final AbstractFastChooseWidget widget;

    public ModernChooseWheel(AbstractFastChooseWidget widget){
        this.widget = widget;
        elements.add( new FastChooseElement(0, 0.0f));
        elements.add( new FastChooseElement(1, 67.5f-22.5f));
        elements.add( new FastChooseElement(2, 112.5f-22.5f));
        elements.add( new FastChooseElement(3, 157.5f-22.5f));
        elements.add( new FastChooseElement(4, 202.5f-22.5f));
        elements.add( new FastChooseElement(5, 247.5f-22.5f));
        elements.add( new FastChooseElement(6, 292.5f-22.5f));
        elements.add( new FastChooseElement(7, 337.5f-22.5f));
    }

    @Nullable
    protected FastChooseElement getActivePart(int mouseX, int mouseY) {
        int x = mouseX - widget.x - widget.size / 2;
        int y = mouseY - widget.y - widget.size / 2;
        int i = 0;
        double pi = Math.PI;

        double distanceFromCenter = Math.sqrt(x * x + y * y);
        if (distanceFromCenter < widget.size * 0.17 || distanceFromCenter > widget.size / 2.0) {
            return null;
        }

        float degrees = (float) (Math.abs(((Math.atan2(y, x) - pi) / (2 * pi)) * 360 - 270) % 360);
        if (degrees < 22.5) {
            i = 0;
        } else if (degrees < 22.5 + 45) {
            i = 1;
        } else if (degrees < 22.5 + 90) {
            i = 2;
        } else if (degrees < 22.5 + 135) {
            i = 3;
        } else if (degrees < 22.5 + 180) {
            i = 4;
        } else if (degrees < 22.5 + 225) {
            i = 5;
        } else if (degrees < 22.5 + 270) {
            i = 6;
        } else if (degrees < 22.5 + 315) {
            i = 7;
        }
        return elements.get(i);
    }

    private int getPageButton(int mouseX, int mouseY) {
        int x = mouseX - widget.x - widget.size / 2;
        int y = mouseY - widget.y - widget.size / 2;
        double distanceFromCenter = Math.sqrt(x * x + y * y);
        if (distanceFromCenter < widget.size * 0.17 || distanceFromCenter > widget.size / 2.0) {
            if (x > 1) {
                return 1;
            } else if (x < 1) {
                return 0;
            }
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        checkHovered(mouseX, mouseY);
        //widget.renderBindTexture(TEXTURE);
        RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, TEXTURE, 0, 0, 0, 0, 2);
        if(this.hovered){
            FastChooseElement part = getActivePart(mouseX, mouseY);
            if(part != null && widget.doHoverPart(part)){
                part.renderHover(matrices, TEXTURE);
            }
        }
        for(FastChooseElement f : elements){
            if(f.hasEmote()) f.render(matrices);
        }
        Component text = Component.literal(String.valueOf(fastMenuPage + 1));
        matrices.drawString(Minecraft.getInstance().font, text, (int) (widget.x + widget.size / 2f - 2), (int) (widget.y + widget.size / 2f - 3), -1);
    }


    /**
     * @param matrices MatrixStack ...
     * @param x        Render x from this pixel
     * @param y        same
     * @param u        texture x
     * @param v        texture y
     * @param s        used texture part size !NOT THE WHOLE TEXTURE IMAGE SIZE!
     */
    private void drawTexture(GuiGraphics matrices, ResourceLocation t, int x, int y, int u, int v, int s){
        matrices.blit(t, widget.x + x * widget.size / 256, widget.y + y * widget.size / 256, s * widget.size / 2, s * widget.size / 2, (float) u, (float) v, s * 128, s * 128, 512, 512);
    }
    private void drawTexture_select(GuiGraphics matrices, ResourceLocation t, int x, int y, int u, int v, int w, int h){
        matrices.blit(t, widget.x + x * widget.size / 512, widget.y + y * widget.size / 512, w * widget.size / 2, h * widget.size / 2, (float) u, (float) v, w * 128, h * 128, 512, 512);
    }

    private void checkHovered(int mouseX, int mouseY){
        this.hovered = mouseX >= widget.x && mouseY >= widget.y && mouseX <= widget.x + widget.size && mouseY <= widget.y + widget.size;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkHovered((int) mouseX, (int) mouseY);
        if(this.hovered && widget.isValidClickButton(button)){
            FastChooseElement element = this.getActivePart((int) mouseX, (int) mouseY);
            if(element != null){
                return widget.onClick(element, button);
            } else {
                int selectedPageButton = getPageButton((int) mouseX, (int) mouseY);
                if (selectedPageButton == 0) {
                    if (fastMenuPage > 0) {
                        fastMenuPage -= 1;
                    } else {
                        fastMenuPage = 9;
                    }
                } else if (selectedPageButton == 1) {
                    if (fastMenuPage < 9) {
                        fastMenuPage += 1;
                    } else {
                        fastMenuPage = 0;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        checkHovered((int) mouseX, (int) mouseY);
        if (amount < 0) {
            if (fastMenuPage < 9) {
                fastMenuPage++;
                return true;
            }
        } else {
            if (fastMenuPage > 0) {
                fastMenuPage--;
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY){
        this.checkHovered((int) mouseX, (int) mouseY);
        return this.hovered;
    }


    protected class FastChooseElement implements IChooseWheel.IChooseElement {
        private final float angle;
        private final int id;

        protected FastChooseElement(int num, float angle){
            this.angle = angle;
            this.id = num;
        }

        @Override
        public boolean hasEmote(){
            return ((ClientConfig)EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null;
        }

        @Override
        public void setEmote(@Nullable EmoteHolder emote){
            ((ClientConfig)EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] = emote == null ? null : emote.getUuid();
        }

        @Override
        @Nullable
        public EmoteHolder getEmote(){
            UUID uuid = ((ClientConfig)EmoteInstance.config).fastMenuEmotes[fastMenuPage][id];
            if(uuid != null){
                EmoteHolder emote = EmoteHolder.list.get(uuid);
                if(emote == null && widget.doesShowInvalid()){
                    emote = new EmoteHolder.Empty(uuid);
                }
                return emote;
            }
            else {
                return null;
            }
        }

        @Override
        public void clearEmote(){
            this.setEmote(null);
        }

        public void render(GuiGraphics matrices){
            UUID emoteID = ((ClientConfig)EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null ? ((ClientConfig)EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] : null;
            ResourceLocation identifier = emoteID != null && EmoteHolder.list.get(emoteID) != null ? EmoteHolder.list.get(emoteID).getIconIdentifier() : null;
            if (identifier != null) {//TODO ADD ICON & TEXT RENDER
                switch ((SerializableConfig.Icon) ((ClientConfig) EmoteInstance.config).showIcons.get()) {
                    case ICON -> renderIcon(matrices,identifier);
                    case ICON_TEXT -> {
                        renderIcon(matrices,identifier);
                        renderText(matrices, fastMenuPage,1.6);
                    }
                    case TEXT -> renderText(matrices, fastMenuPage,2.0);
                }
            } else {
                if(!renderText(matrices, fastMenuPage,2.0)) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Tried to render non-existing name", true);
                }
            }
        }

        private boolean renderText(GuiGraphics matrices, int fastMenuPage, double offset) {
            if (((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null) {
                widget.drawCenteredText(matrices, EmoteHolder.getNonNull(((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id]).name, this.angle,offset);
            }
            return false;
        }

        private void renderIcon(GuiGraphics matrices, ResourceLocation identifier) {
            int s = widget.size / 10;
            int iconX = (int) (((float) (widget.x + widget.size / 2)) + widget.size * 0.4 * Math.sin(this.angle * 0.0174533)) - s;
            int iconY = (int) (((float) (widget.y + widget.size / 2)) + widget.size * 0.4 * Math.cos(this.angle * 0.0174533)) - s;
            //widget.renderBindTexture(identifier);
            matrices.blit(identifier, iconX, iconY, s * 2, s * 2, (float) 0, (float) 0, 256, 256, 256, 256);
        }

        public void renderHover(GuiGraphics matrices, ResourceLocation t){
            switch (id) {
                case 0:
                    drawTexture_select(matrices, t,0, 256, 0, 384, 2, 1);//0
                    break;
                case 1:
                    drawTexture_select(matrices, t,256, 256, 384, 384, 1, 1);//1
                    break;
                case 2:
                    drawTexture_select(matrices, t,256, 0, 384, 0, 1, 2);//2
                    break;
                case 3:
                    drawTexture_select(matrices, t,256, 0, 384, 256, 1, 1);//3
                    break;
                case 4:
                    drawTexture_select(matrices, t,0, 0, 0, 256, 2, 1);//4
                    break;
                case 5:
                    drawTexture_select(matrices, t,0, 0, 256, 256, 1, 1); //5
                    break;
                case 6:
                    drawTexture_select(matrices, t,0, 0, 256, 0, 1, 2);//6
                    break;
                case 7:
                    drawTexture_select(matrices, t,0, 256, 256, 384, 1, 1);//7
                    break;
            }
        }
    }
}
