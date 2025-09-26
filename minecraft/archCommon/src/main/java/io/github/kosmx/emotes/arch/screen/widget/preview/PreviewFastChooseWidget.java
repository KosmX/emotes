package io.github.kosmx.emotes.arch.screen.widget.preview;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.preview.elemets.PlayerChooseCircleElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.elemets.PlayerChooseSquareElement;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PreviewFastChooseWidget extends AbstractFastChooseWidget {
    public static final ResourceLocation LIGHT_TEXTURE = McUtils.newIdentifier("textures/gui/fastchoose_light_new.png");
    public static final ResourceLocation DARK_TEXTURE = McUtils.newIdentifier("textures/gui/fastchoose_dark_new.png");

    protected final boolean oldChooseWheel = PlatformTools.getConfig().oldChooseWheel.get();

    private final boolean animated;
    private float animTime = 1.0F;

    public PreviewFastChooseWidget(FastChooseController controller, boolean animated, int x, int y, int size) {
        super(controller, x, y, size, CommonComponents.EMPTY);
        this.animated = animated;

        GameProfile profile = Minecraft.getInstance().getGameProfile();
        if (this.oldChooseWheel) {
            this.elements.add(new PlayerChooseCircleElement(this, profile, 0, 0F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 1, 45F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 2, 90F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 3, 135F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 4, 180f));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 5, 225F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 6, 270F));
            this.elements.add(new PlayerChooseCircleElement(this, profile, 7, 315F));
        } else {
            this.elements.add(new PlayerChooseSquareElement(this, profile, 0, -1, -1));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 1, 0, -1));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 2, 1, -1));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 3, -1, 0));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 4, 1, 0));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 5, -1, 1));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 6, 0, 1));
            this.elements.add(new PlayerChooseSquareElement(this, profile, 7, 1, 1));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.oldChooseWheel) {
            drawTexture(this, guiGraphics, PlatformTools.getConfig().dark.get() ? DARK_TEXTURE : LIGHT_TEXTURE, 256, 0, 0, 0, 0, 2, 2);
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void tick() {
        for (AbstractWidget widget : this.elements) {
            if (widget instanceof PlayerPreview preview) preview.tick();
        }
        if (this.animated) {
            this.animTime = Math.max(0.0F, this.animTime - 0.15F);
        }
    }

    public static void drawTexture(LayoutElement widget, GuiGraphics matrices, ResourceLocation texture, int size, int x, int y, int u, int v, int w, int h) {
        matrices.blit(RenderPipelines.GUI_TEXTURED, texture, widget.getX() + x * widget.getWidth() / size, widget.getY() + y * widget.getHeight() / size, u, v, w * widget.getWidth() / 2, h * widget.getHeight() / 2, w * 128, h * 128, 512, 512);
    }

    public float getAnimTime() {
        return this.animated ? this.animTime : 0.0F;
    }
}
