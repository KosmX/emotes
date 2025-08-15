package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;

public class PlayerChooseSquareElement extends PlayerChooseElement {
    protected final int dx;
    protected final int dy;

    public PlayerChooseSquareElement(AbstractFastChooseWidget parent, GameProfile profile, int id, int dx, int dy) {
        super(parent, profile, id);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    protected void updateRectangle() {
        int s = this.parent.globalPadding();
        float distance = this.parent.getWidth() * 0.36f;
        int iconX = (int) (parent.getX() + parent.getWidth() / 2f + this.dx * distance) - s;
        int iconY = (int) (parent.getY() + parent.getHeight() / 2f + this.dy * distance) - s;

        setRectangle(s * 2, s * 2, iconX, iconY);
    }

    @Override
    protected void renderHover(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), CommonColors.GREEN);
    }
}
