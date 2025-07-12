package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class LegacyChooseWidget implements IChooseWheel {

    //protected final FastChooseElement[] elements = new FastChooseElement[8];
    protected final ArrayList<FastChooseElement> elements = new ArrayList<>();
    private boolean hovered;
    private final ResourceLocation TEXTURE = PlatformTools.getConfig().dark.get() ? McUtils.newIdentifier("textures/gui/fastchoose_dark.png") : McUtils.newIdentifier("textures/gui/fastchoose_light.png");

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


    public void drawCenteredText(GuiGraphics matrixStack, Component stringRenderable, float deg) {
        drawCenteredText(matrixStack, stringRenderable, (float) (((float) (widget.getX() + widget.getWidth() / 2)) + widget.getWidth() * 0.4 * Math.sin(deg * 0.0174533)), (float) (((float) (widget.getY() + widget.getHeight() / 2)) + widget.getHeight() * 0.4 * Math.cos(deg * 0.0174533)));
    }

    public void drawCenteredText(GuiGraphics matrices, Component stringRenderable, float x, float y) {
        int c = PlatformTools.getConfig().dark.get() ? 255 : 0; //:D
        float x1 = x - (float) Minecraft.getInstance().font.width(stringRenderable) / 2;
        matrices.drawString(Minecraft.getInstance().font, stringRenderable, (int) x1, (int) (y - 2), ARGB.color(1, c, c, c));
    }

    @Nullable
    protected FastChooseElement getActivePart(int mouseX, int mouseY) {
        int x = mouseX - widget.getX() - widget.getWidth() / 2;
        int y = mouseY - widget.getY() - widget.getHeight() / 2;
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
        // RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
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
        matrices.blit(RenderPipelines.GUI_TEXTURED, texture, widget.getX() + x * widget.getWidth() / 256, widget.getY() + y * widget.getHeight() / 256, u, v, s * widget.getWidth() / 2, s * widget.getHeight() / 2, s * 128, s * 128, 512, 512);
    }

    private void checkHovered(int mouseX, int mouseY) {
        this.hovered = mouseX >= widget.getX() && mouseY >= widget.getY() && mouseX <= widget.getX() + widget.getWidth() && mouseY <= widget.getY() + widget.getHeight();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
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
            return PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id] != null;
        }

        @Override
        public void setEmote(@Nullable EmoteHolder emote) {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id] = emote == null ? null : emote.getUuid();
        }

        @Nullable
        @Override
        public EmoteHolder getEmote() {
            int fastMenuPage = ModernChooseWheel.fastMenuPage;
            UUID uuid = PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id];
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
            UUID emoteID = PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id] != null ? PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id] : null;
            ResourceLocation identifier = emoteID != null && EmoteHolder.list.get(emoteID) != null ? EmoteHolder.list.get(emoteID).getIconIdentifier() : null;
            if (identifier != null && PlatformTools.getConfig().showIcons.get()) {
                int s = widget.getWidth() / 10;
                int iconX = (int) (((float) (widget.getX() + widget.getWidth() / 2)) + widget.getWidth() * 0.4 * Math.sin(this.angle * 0.0174533)) - s;
                int iconY = (int) (((float) (widget.getY() + widget.getHeight() / 2)) + widget.getHeight() * 0.4 * Math.cos(this.angle * 0.0174533)) - s;
                //widget.renderBindTexture(identifier);
                matrices.blit(RenderPipelines.GUI_TEXTURED, identifier, iconX, iconY, 0.0F, 0.0F, s * 2, s * 2, 256, 256, 256, 256);
            } else {
                if (PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id] != null) {
                    drawCenteredText(matrices, EmoteHolder.getNonNull(PlatformTools.getConfig().fastMenuEmotes[fastMenuPage][id]).name, this.angle);
                } else {
                    CommonData.LOGGER.warn("Tried to render non-existing name");
                }
            }
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
