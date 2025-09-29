package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.sounds.SoundManager;

import java.time.Duration;
import java.util.UUID;

public abstract class PlayerChooseElement extends PlayerPreview implements IChooseElement {
    protected final AbstractFastChooseWidget parent;
    protected final int id;

    public PlayerChooseElement(AbstractFastChooseWidget parent, GameProfile profile, int id) {
        super(profile, 0, 0, 0, 0, false);
        this.player.emotecraft$getEmote().muteNbs = true;

        this.parent = parent;
        this.id = id;

        tick();
    }

    protected abstract void updateRectangle();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = this.isHovered && this.parent.controller.doHoverPart(this);

        updateRectangle();
        if (isHoveredOrFocused()) renderHover(guiGraphics);

        EmoteHolder emoteHolder = getEmote();
        /*Optional<ResourceLocation> icon = Optional.ofNullable(emoteHolder).map(EmoteHolder::getIconIdentifier);

        if (PlatformTools.getConfig().showIconsIfPossible.get() && icon.isPresent()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.orElseThrow(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else*/ {
            super.renderWidget(guiGraphics, getX() + (getWidth() / 2), getY() + (getHeight() / 2), partialTick);
        }

        if (isHoveredOrFocused() && emoteHolder != null) {
            setTooltip(Tooltip.create(emoteHolder.name));
            setTooltipDelay(Duration.ZERO);
        } else setTooltip(null);
    }

    protected abstract void renderHover(GuiGraphics guiGraphics);

    @Override
    public void removed() {
        this.player.stopEmote();
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

        boolean updated = playAnimation(holder == null ? null : holder.getEmote(), true, previewTick);

        super.pause(!updated && !isHoveredOrFocused());
        if (updated || isHoveredOrFocused()) super.tick();
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

    @Override
    public void playDownSound(SoundManager handler) {
        playButtonClickSound(handler);
    }
}
