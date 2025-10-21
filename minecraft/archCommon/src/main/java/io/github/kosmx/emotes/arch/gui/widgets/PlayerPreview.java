package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.easing.EasingType;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class PlayerPreview extends AbstractWidget implements LayoutElement {
    private static final Float2FloatFunction EASING_TRANSFORMER = EasingType.EASE_OUT_QUART.buildTransformer(null);

    protected final boolean renderBackround;
    protected ClientMannequin mannequin;

    protected float animTime = 1.0F;

    public PlayerPreview(GameProfile profile, int x, int y, int width, int height, boolean renderBackround) {
        super(x, y, width, height, CommonComponents.EMPTY);

        this.mannequin = new UnsafeMannequin(null, profile);
        this.renderBackround = renderBackround;
        setAlpha(0.0F);
    }

    public boolean playAnimation(@Nullable Animation animation, Animation.LoopType loopType, boolean check) {
        return playAnimation(animation, loopType, check, 0);
    }

    public boolean playAnimation(@Nullable Animation animation, Animation.LoopType loopType, boolean check, float tick) {
        if (check && animation != null) {
            EmotePlayer emotePlayer = this.mannequin.emotecraft$getEmote();
            if (animation.equals(emotePlayer.getCurrentAnimationInstance())) {
                return false;
            }
        }
        this.mannequin.emotecraft$playEmote(animation, loopType, tick, check);
        return true;
    }

    public void pause(boolean paused) {
        EmotePlayer emotePlayer = this.mannequin.emotecraft$getEmote();
        if (paused) {
            emotePlayer.pause();
        } else {
            emotePlayer.unpause();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushMatrix();

        if (this.renderBackround) {
            float alpha = Mth.lerp(this.alpha, 0.0F, 0.5F);
            guiGraphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, ARGB.colorFromFloat(
                    alpha, 0.0F, 0.0F, 0.0F
            ));
            WidgetOutliner.renderOutline(guiGraphics, this, ARGB.white(alpha));
        }

        guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());

        try {
            int scale = getHeight() / (this.renderBackround ? 3 : 2);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, getX(), getY(), getRight(), getBottom(), Mth.lerpInt(this.alpha, 0, scale), 0.0625F, mouseX, mouseY, this.mannequin);
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to render entity preview!", th);
        }

        guiGraphics.disableScissor();
        guiGraphics.pose().popMatrix();

        if (this.animTime > 0.0F) {
            setAlpha(1.0F - EASING_TRANSFORMER.get(this.animTime));
        }

        if (isHovered()) {
            guiGraphics.requestCursor(isActive() && this.mannequin.isPlayingEmote() ? CursorTypes.RESIZE_ALL : CursorType.DEFAULT);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public void tick() {
        if (this.visible && this.mannequin != null && this.mannequin.isPlayingEmote()) {
            this.animTime = 0.0F;
            setAlpha(1.0F);

            try {
                this.mannequin.tick();
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to tick entity preview!", th);
            }
        } else {
            this.animTime = Math.min(1.0F, this.animTime + 0.1F);
        }
    }

    public ClientMannequin getMannequin() {
        return this.mannequin;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // no-op
    }
}
