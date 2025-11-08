package io.github.kosmx.emotes.arch.screen.widget.preview;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.preview.elemets.PlayerChooseCircleElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.elemets.PlayerChooseSquareElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

public class PreviewFastChooseWidget extends AbstractFastChooseWidget {
    private final boolean animated;
    private float animTime = 1.0F;

    public PreviewFastChooseWidget(FastChooseController controller, boolean animated, int x, int y, int size) {
        super(controller, x, y, size, CommonComponents.EMPTY);
        this.animated = animated;

        GameProfile profile = Minecraft.getInstance().getGameProfile();
        if (PlatformTools.getConfig().oldChooseWheel.get()) {
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

    public float getAnimTime() {
        return this.animated ? this.animTime : 0.0F;
    }
}
