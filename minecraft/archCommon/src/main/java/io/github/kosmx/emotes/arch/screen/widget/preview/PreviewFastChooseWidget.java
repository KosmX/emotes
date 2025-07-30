package io.github.kosmx.emotes.arch.screen.widget.preview;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PreviewFastChooseWidget extends AbstractFastChooseWidget {
    protected static final ResourceLocation LIGHT_TEXTURE = McUtils.newIdentifier("textures/gui/fastchoose_light_new.png");
    protected static final ResourceLocation DARK_TEXTURE = McUtils.newIdentifier("textures/gui/fastchoose_light_new.png");

    public PreviewFastChooseWidget(FastChooseController controller, int x, int y, int size) {
        super(controller, x, y, size, Component.empty());

        GameProfile profile = Minecraft.getInstance().getGameProfile();
        this.elements.add(new PlayerChooseElement(this, profile, 0, 0F));
        this.elements.add(new PlayerChooseElement(this, profile, 1, 45F));
        this.elements.add(new PlayerChooseElement(this, profile, 2, 90F));
        this.elements.add(new PlayerChooseElement(this, profile, 3, 135F));
        this.elements.add(new PlayerChooseElement(this, profile, 4, 180f));
        this.elements.add(new PlayerChooseElement(this, profile, 5, 225F));
        this.elements.add(new PlayerChooseElement(this, profile, 6, 270F));
        this.elements.add(new PlayerChooseElement(this, profile, 7, 315F));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (PlatformTools.getConfig().oldChooseWheel.get()) {
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
            if (widget instanceof PlayerChooseElement preview) preview.tick();
        }
    }

    public static void drawTexture(LayoutElement widget, GuiGraphics matrices, ResourceLocation texture, int size, int x, int y, int u, int v, int w, int h) {
        matrices.blit(RenderPipelines.GUI_TEXTURED, texture, widget.getX() + x * widget.getWidth() / size, widget.getY() + y * widget.getHeight() / size, u, v, w * widget.getWidth() / 2, h * widget.getHeight() / 2, w * 128, h * 128, 512, 512);
    }
}
