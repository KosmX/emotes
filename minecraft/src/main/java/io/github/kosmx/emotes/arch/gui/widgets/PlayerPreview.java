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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class PlayerPreview extends AbstractWidget implements LayoutElement {
    private static final Float2FloatFunction EASING_TRANSFORMER = EasingType.EASE_OUT_QUART.buildTransformer(null);

    protected final boolean renderBackground;
    protected ClientMannequin mannequin;

    protected float animTime = 1.0F;

    public PlayerPreview(GameProfile profile, int x, int y, int width, int height, boolean renderBackground) {
        super(x, y, width, height, CommonComponents.EMPTY);

        this.mannequin = new UnsafeMannequin(null, profile);
        this.renderBackground = renderBackground;
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.pose().pushMatrix();

        if (this.renderBackground) {
            float alpha = Mth.lerp(this.alpha, 0.0F, 0.5F);
            graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, ARGB.colorFromFloat(
                    alpha, 0.0F, 0.0F, 0.0F
            ));
            WidgetOutliner.extractOutline(graphics, this, ARGB.white(alpha));
        }

        graphics.enableScissor(getX(), getY(), getRight(), getBottom());

        try {
            int scale = getHeight() / (this.renderBackground ? 3 : 2);
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, getX(), getY(), getRight(), getBottom(), Mth.lerpInt(this.alpha, 0, scale), 0.0625F, mouseX, mouseY, this.mannequin);
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to render entity preview!", th);
        }

        graphics.disableScissor();
        graphics.pose().popMatrix();

        if (this.animTime > 0.0F) {
            setAlpha(1.0F - EASING_TRANSFORMER.get(this.animTime));
        }

        if (isHovered()) {
            graphics.requestCursor(isActive() && this.mannequin.isPlayingEmote() ? CursorTypes.RESIZE_ALL : CursorType.DEFAULT);
        }
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
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
    public void playDownSound(@NonNull SoundManager handler) {
        // no-op
    }
}
