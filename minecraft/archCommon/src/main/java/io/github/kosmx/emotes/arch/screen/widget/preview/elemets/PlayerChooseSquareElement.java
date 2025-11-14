package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;

public class PlayerChooseSquareElement extends PlayerChooseElement {
    protected final int dx;
    protected final int dy;

    public PlayerChooseSquareElement(AbstractFastChooseWidget parent, GameProfile profile, int id, int dx, int dy) {
        super(parent, profile, id);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    protected void updateRectangle(float easedProgress) {
        int s = this.parent.globalPadding();
        float distance = (s * 2.5f) * easedProgress;
        int iconX = Math.round(parent.getX() + parent.getWidth() / 2F + this.dx * distance) - s;
        int iconY = Math.round(parent.getY() + parent.getHeight() / 2F + this.dy * distance) - s;

        setRectangle(s * 2, s * 2, iconX, iconY);
    }
}
