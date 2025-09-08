package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranimcore.easing.EasingType;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import static net.minecraft.client.gui.components.AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND;
import static net.minecraft.client.gui.components.AbstractSelectionList.MENU_LIST_BACKGROUND;

public class PlayerChooseSquareElement extends PlayerChooseElement {
    private static final Float2FloatFunction EASING_TRANSFORMER = EasingType.EASE_IN_CIRC.buildTransformer(null);

    protected final int dx;
    protected final int dy;

    private boolean isAnimFinishing;

    public PlayerChooseSquareElement(AbstractFastChooseWidget parent, GameProfile profile, int id, int dx, int dy) {
        super(parent/*, profile*/, id);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    protected void updateRectangle() {
        float progress = this.parent instanceof PreviewFastChooseWidget widget ? widget.getAnimTime() : 0.0F;
        float easedProgress = 1.0F - EASING_TRANSFORMER.get(progress);
        this.isAnimFinishing = easedProgress > 0.9F;

        int s = this.parent.globalPadding();
        float distance = this.parent.getWidth() * 0.36f * easedProgress;
        int iconX = (int) (parent.getX() + parent.getWidth() / 2f + this.dx * distance) - s;
        int iconY = (int) (parent.getY() + parent.getHeight() / 2f + this.dy * distance) - s;

        setRectangle(s * 2, s * 2, iconX, iconY);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = Minecraft.getInstance().level == null ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), getRight(), getBottom(), getWidth(), getHeight(), 32, 32);

        super.renderWidget(guiGraphics, this.isAnimFinishing ? mouseX : 0, this.isAnimFinishing ? mouseY : 0, partialTick);
    }

    @Override
    protected void renderHover(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.color(128, 66, 66, 66));
    }
}
