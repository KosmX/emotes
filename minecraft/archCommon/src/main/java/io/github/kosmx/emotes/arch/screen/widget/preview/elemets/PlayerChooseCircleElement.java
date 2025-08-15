package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget.drawTexture;

public class PlayerChooseCircleElement extends PlayerChooseElement {
    protected final float angle;

    public PlayerChooseCircleElement(AbstractFastChooseWidget parent, GameProfile profile, int id, float angle) {
        super(parent, profile, id);
        this.angle = angle;
    }

    @Override
    protected void updateRectangle() {
        int s = this.parent.globalPadding();
        int iconX = (int) (((float) (parent.getX() + parent.getWidth() / 2)) + parent.getWidth() * 0.36 * Math.sin(this.angle * 0.0174533)) - s;
        int iconY = (int) (((float) (parent.getY() + parent.getHeight() / 2)) + parent.getHeight() * 0.36 * Math.cos(this.angle * 0.0174533)) - s;
        setRectangle(s * 2, s * 2, iconX, iconY);
    }

    @Override
    protected void renderHover(GuiGraphics matrices) {
        if (!PlatformTools.getConfig().oldChooseWheel.get()) return;
        ResourceLocation texture = PlatformTools.getConfig().dark.get() ? PreviewFastChooseWidget.DARK_TEXTURE : PreviewFastChooseWidget.LIGHT_TEXTURE;

        switch (this.id) {
            case 0:
                drawTexture(this, matrices, texture, 512, 0, 256, 0, 384, 2, 1); // 0
                break;
            case 1:
                drawTexture(this, matrices, texture, 512, 256, 256, 384, 384, 1, 1); // 1
                break;
            case 2:
                drawTexture(this, matrices, texture, 512, 256, 0, 384, 0, 1, 2); // 2
                break;
            case 3:
                drawTexture(this, matrices, texture, 512, 256, 0, 384, 256, 1, 1); // 3
                break;
            case 4:
                drawTexture(this, matrices, texture, 512, 0, 0, 0, 256, 2, 1); // 4
                break;
            case 5:
                drawTexture(this, matrices, texture, 512, 0, 0, 256, 256, 1, 1); // 5
                break;
            case 6:
                drawTexture(this, matrices, texture, 512, 0, 0, 256, 0, 1, 2);// 6
                break;
            case 7:
                drawTexture(this, matrices, texture, 512, 0, 256, 256, 384, 1, 1);// 7
                break;
        }
    }
}
