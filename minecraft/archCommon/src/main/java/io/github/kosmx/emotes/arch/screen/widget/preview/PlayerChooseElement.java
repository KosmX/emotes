package io.github.kosmx.emotes.arch.screen.widget.preview;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget.drawTexture;

public class PlayerChooseElement extends PlayerPreview implements IChooseElement {
    protected final AbstractFastChooseWidget parent;
    protected final float angle;
    protected final int id;

    public PlayerChooseElement(AbstractFastChooseWidget parent, GameProfile profile, int num, float angle) {
        super(profile, 0, 0, 0, 0, false);

        this.parent = parent;
        this.angle = angle;
        this.id = num;
        setAlpha(1.0F);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = this.isHovered && this.parent.controller.doHoverPart(this);

        int s = parent.globalPadding();
        int iconX = (int) (((float) (parent.getX() + parent.getWidth() / 2)) + parent.getWidth() * 0.36 * Math.sin(this.angle * 0.0174533)) - s;
        int iconY = (int) (((float) (parent.getY() + parent.getHeight() / 2)) + parent.getHeight() * 0.36 * Math.cos(this.angle * 0.0174533)) - s;
        setRectangle(s * 2, s * 2, iconX, iconY);

        if (isHoveredOrFocused() && PlatformTools.getConfig().oldChooseWheel.get()) {
            ResourceLocation texture = PlatformTools.getConfig().dark.get() ? PreviewFastChooseWidget.DARK_TEXTURE : PreviewFastChooseWidget.LIGHT_TEXTURE;
            renderHover(parent, guiGraphics, texture, id);
        }

        Optional<ResourceLocation> icon = Optional.ofNullable(getEmote()).map(EmoteHolder::getIconIdentifier);
        if (PlatformTools.getConfig().showIconsIfPossible.get() && icon.isPresent()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.orElseThrow(), iconX, iconY, 0.0F, 0.0F, s * 2, s * 2, 256, 256, 256, 256);
        } else {
            super.renderWidget(guiGraphics, getX() + (getWidth() / 2), getY() + (getHeight() / 2), partialTick);
        }

        if (isHoveredOrFocused() && hasEmote()) {
            EmoteHolder emoteHolder = getEmote();
            if (emoteHolder != null) {
                setTooltip(Tooltip.create(emoteHolder.name));
                setTooltipDelay(Duration.ZERO);
            }
        }
    }

    @Override
    public boolean hasEmote() {
        return PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id] != null;
    }

    @Override
    public EmoteHolder getEmote() {
        UUID uuid = PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id];
        if (uuid != null) {
            EmoteHolder emote = EmoteHolder.list.get(uuid);
            if (emote == null && this.parent.controller.doesShowInvalid()) {
                emote = new EmoteHolder.Empty(uuid);
            }
            return emote;
        } else {
            return null;
        }
    }

    @Override
    public void clearEmote() {
        setEmote(null);
    }

    @Override
    public void setEmote(EmoteHolder emote) {
        PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id] = emote == null ? null : emote.getUuid();
    }

    @Override
    public void tick() {
        EmoteHolder holder = getEmote();
        if (holder != null) playAnimation(holder.getEmote(), true);
        super.tick();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return this.parent.controller.isValidClickButton(button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return this.parent.controller.onClick(this, button);
        }
        return false;
    }

    public static void renderHover(LayoutElement widget, GuiGraphics matrices, ResourceLocation texture, int id) {
        switch (id) {
            case 0:
                drawTexture(widget, matrices, texture, 512, 0, 256, 0, 384, 2, 1); // 0
                break;
            case 1:
                drawTexture(widget, matrices, texture, 512, 256, 256, 384, 384, 1, 1); // 1
                break;
            case 2:
                drawTexture(widget, matrices, texture, 512, 256, 0, 384, 0, 1, 2); // 2
                break;
            case 3:
                drawTexture(widget, matrices, texture, 512, 256, 0, 384, 256, 1, 1); // 3
                break;
            case 4:
                drawTexture(widget, matrices, texture, 512, 0, 0, 0, 256, 2, 1); // 4
                break;
            case 5:
                drawTexture(widget, matrices, texture, 512, 0, 0, 256, 256, 1, 1); // 5
                break;
            case 6:
                drawTexture(widget, matrices, texture, 512, 0, 0, 256, 0, 1, 2);// 6
                break;
            case 7:
                drawTexture(widget, matrices, texture, 512, 0, 256, 256, 384, 1, 1);// 7
                break;
        }
    }
}
