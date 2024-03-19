package io.github.kosmx.emotes.arch.screen.widget;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.util.MathHelper;
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

public class LegacyChooseWidget implements IChooseWheel {

    //protected final FastChooseElement[] elements = new FastChooseElement[8];
    protected final ArrayList<FastChooseElement> elements = new ArrayList<>();
    private boolean hovered;
    private final ResourceLocation TEXTURE = ((ClientConfig) EmoteInstance.config).dark.get() ? PlatformTools.newIdentifier("textures/gui/fastchoose_dark.png") : PlatformTools.newIdentifier("textures/gui/fastchoose_light.png");

    private final AbstractFastChooseWidget widget;

    public LegacyChooseWidget(AbstractFastChooseWidget widget) {
        this.widget = widget;
        elements.add(new FastChooseElement(0, 22.5f));
        elements.add(new FastChooseElement(1, 67.5f));
        elements.add(new FastChooseElement(2, 157.5f));
        elements.add(new FastChooseElement(3, 112.5f));
        elements.add(new FastChooseElement(4, 337.5f));
        elements.add(new FastChooseElement(5, 292.5f));
        elements.add(new FastChooseElement(6, 202.5f));
        elements.add(new FastChooseElement(7, 247.5f));
    }


    public void drawCenteredText(GuiGraphics matrixStack, Component stringRenderable, float deg, double offset) {
        drawCenteredText(matrixStack, stringRenderable, (float) (((float) (widget.x + widget.size / 2)) + widget.size * 0.4 * Math.sin(deg * 0.0174533)), (float) (((float) (widget.y + widget.size / offset)) + widget.size * 0.4 * Math.cos(deg * 0.0174533)));
    }

    public void drawCenteredText(GuiGraphics matrices, Component stringRenderable, float x, float y) {
        int c = 255; //TODO ONLY WALTER WHITE //:D
        float x1 = x - (float) Minecraft.getInstance().font.width(stringRenderable) / 2;
        matrices.drawString(Minecraft.getInstance().font, stringRenderable, (int) x1, (int) (y - 2), MathHelper.colorHelper(c, c, c, 1));
    }

    @Nullable
    protected FastChooseElement getActivePart(int mouseX, int mouseY) {
        int x = mouseX - widget.x - widget.size / 2;
        int y = mouseY - widget.y - widget.size / 2;
        int i = 0;
        if (x == 0) {
            return null;
        } else if (x < 0) {
            i += 4;
        }

        if (y == 0) {
            return null;
        } else if (y < 0) {
            i += 2;
        }

        if (Math.abs(x) == Math.abs(y)) {
            return null;
        } else if (Math.abs(x) > Math.abs(y)) {
            i++;
        }
        return elements.get(i);
    }

    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        checkHovered(mouseX, mouseY);
        //widget.renderBindTexture(TEXTURE);
        RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.drawTexture(matrices, TEXTURE, 0, 0, 0, 0, 2);
        if (this.hovered) {
            FastChooseElement part = getActivePart(mouseX, mouseY);
            if (part != null && widget.doHoverPart(part)) {
                part.renderHover(matrices, TEXTURE);
            }
        }
        for (FastChooseElement f : elements) {
            if (f.hasEmote()) f.render(matrices);
        }
    }

    /**
     * @param matrices MatrixStack ...
     * @param x        Render x from this pixel
     * @param y        same
     * @param u        texture x
     * @param v        texture y
     * @param s        used texture part size !NOT THE WHOLE TEXTURE IMAGE SIZE!
     */
    private void drawTexture(GuiGraphics matrices, ResourceLocation texture, int x, int y, int u, int v, int s) {
        matrices.blit(texture, widget.x + x * widget.size / 256, widget.y + y * widget.size / 256, s * widget.size / 2, s * widget.size / 2, (float) u, (float) v, s * 128, s * 128, 512, 512);
    }

    private void checkHovered(int mouseX, int mouseY) {
        this.hovered = mouseX >= widget.x && mouseY >= widget.y && mouseX <= widget.x + widget.size && mouseY <= widget.y + widget.size;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkHovered((int) mouseX, (int) mouseY);
        if (this.hovered && widget.isValidClickButton(button)) {
            FastChooseElement element = this.getActivePart((int) mouseX, (int) mouseY);
            if (element != null) {
                return widget.onClick(element, button);
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }


    public boolean isMouseOver(double mouseX, double mouseY) {
        this.checkHovered((int) mouseX, (int) mouseY);
        return this.hovered;
    }


    protected class FastChooseElement implements IChooseElement {
        private final float angle;
        private final int id;


        protected FastChooseElement(int num, float angle) {
            this.angle = angle;
            this.id = num;
        }

        public boolean hasEmote() {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            return ((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null;
        }

        @Override
        public void setEmote(@Nullable EmoteHolder emote) {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            ((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] = emote == null ? null : emote.getUuid();
        }

        @Nullable
        @Override
        public EmoteHolder getEmote() {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            UUID uuid = ((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id];
            if (uuid != null) {
                EmoteHolder emote = EmoteHolder.list.get(uuid);
                if (emote == null && widget.doesShowInvalid()) {
                    emote = new EmoteHolder.Empty(uuid);
                }
                return emote;
            } else {
                return null;
            }
        }

        @Override
        public void clearEmote() {
            this.setEmote(null);
        }

        public void render(GuiGraphics matrices) {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            UUID emoteID = ((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null ? ((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] : null;
            ResourceLocation identifier = emoteID != null && EmoteHolder.list.get(emoteID) != null ? EmoteHolder.list.get(emoteID).getIconIdentifier() : null;
            if (identifier != null) {//TODO ADD ICON & TEXT RENDER
                switch ((SerializableConfig.Icon) ((ClientConfig) EmoteInstance.config).showIcons.get()) {
                    case ICON -> renderIcon(matrices,identifier);
                    case ICON_TEXT -> {
                        renderIcon(matrices,identifier);
                        renderText(matrices, fastMenuPage,1.6);
                    }
                    case TEXT -> renderText(matrices, fastMenuPage, 2.0);
                }
            } else {
                if(!renderText(matrices, fastMenuPage, 2.0)) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Tried to render non-existing name", true);
                }
            }
        }

        private boolean renderText(GuiGraphics matrices, int fastMenuPage, double offset) {
            if (((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id] != null) {
                drawCenteredText(matrices, EmoteHolder.getNonNull(((ClientConfig) EmoteInstance.config).fastMenuEmotes[fastMenuPage][id]).name, this.angle, offset);
                return true;
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

        public void renderHover(GuiGraphics matrices, ResourceLocation texture) {
            int textX = 0;
            int textY = 0;
            int x = 0;
            int y = 0;

            if ((id & 1) == 0) {
                textY = 256;
            } else {
                textX = 256;
            }

            if ((id & 2) == 0) {
                y = 128;
            }

            if ((id & 4) == 0) {
                x = 128;
            }
            drawTexture(matrices, texture, x, y, textX + x, textY + y, 1);
        }
    }
}
