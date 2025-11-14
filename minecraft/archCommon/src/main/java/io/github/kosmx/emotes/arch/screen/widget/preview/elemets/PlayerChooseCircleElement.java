package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;

public class PlayerChooseCircleElement extends PlayerChooseElement {
    protected final float angle;

    public PlayerChooseCircleElement(AbstractFastChooseWidget parent, GameProfile profile, int id, float angle) {
        super(parent, profile, id);
        this.angle = angle;
    }

    @Override
    protected void updateRectangle(float easedProgress) {
        int s = this.parent.globalPadding();
        int iconX = (int) (((float) (parent.getX() + parent.getWidth() / 2)) + parent.getWidth() * 0.36 * Math.sin(this.angle * 0.0174533) * easedProgress) - s;
        int iconY = (int) (((float) (parent.getY() + parent.getHeight() / 2)) + parent.getHeight() * 0.36 * Math.cos(this.angle * 0.0174533) * easedProgress) - s;
        setRectangle(s * 2, s * 2, iconX, iconY);
    }
}
