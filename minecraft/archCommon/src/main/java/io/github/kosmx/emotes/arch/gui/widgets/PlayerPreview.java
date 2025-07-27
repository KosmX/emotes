package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.easing.EasingType;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class PlayerPreview extends AbstractWidget implements LayoutElement {
    private static final Float2FloatFunction EASING_TRANSFORMER = EasingType.EASE_OUT_QUART.buildTransformer(null);

    protected final boolean renderBackround;
    protected RemotePlayer player;

    protected float animTime = 1.0F;

    public PlayerPreview(GameProfile profile, int x, int y, int width, int height, boolean renderBackround) {
        super(x, y, width, height, Component.empty());

        this.player = new UnsafeRemotePlayer(null, profile);
        this.renderBackround = renderBackround;
        setAlpha(0.0F);
    }

    public void playAnimation(@Nullable Animation animation, boolean check) {
        if (check && animation != null) {
            EmotePlayer emotePlayer = this.player.emotecraft$getEmote();
            if (animation.equals(emotePlayer.getData())) {
                return;
            }
        }
        this.player.emotecraft$playEmote(animation, 0, check);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());

        if (this.renderBackround) {
            guiGraphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.colorFromFloat(
                    Mth.lerp(this.alpha, 0.0F, 0.5F), 0.0F, 0.0F, 0.0F
            ));
        }

        guiGraphics.nextStratum();
        try {
            int scale = this.renderBackround ? getHeight() / 3 : getHeight() / 2;
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, getX(), getY(), getRight(), getBottom(), Mth.lerpInt(this.alpha, 0, scale), 0.0625F, mouseX, mouseY, this.player);
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to render entity preview!", th);
        }

        guiGraphics.disableScissor();
        guiGraphics.pose().popMatrix();

        if (this.animTime > 0.0F) {
            setAlpha(1.0F - EASING_TRANSFORMER.get(this.animTime));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public void tick() {
        if (this.visible && (this.player != null && this.player.isPlayingEmote() || !this.renderBackround)) {
            this.animTime = 0.0F;
            setAlpha(1.0F);

            try {
                this.player.tick();
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to tick entity preview!", th);
            }
        } else {
            this.animTime = Math.min(1.0F, this.animTime + 0.1F);
        }
    }

    public RemotePlayer getPlayer() {
        return this.player;
    }
}
