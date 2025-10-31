package io.github.kosmx.emotes.arch.screen.ingame;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.components.EmoteSubScreen;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FullMenuScreen extends EmoteSubScreen {
    protected static final Component TITLE = Component.translatable("emotecraft.emotelist");
    protected static final Component CONFIG = Component.translatable("emotecraft.config");

    public FullMenuScreen(Screen parent) {
        super(TITLE, false, parent);
    }

    @Override
    protected void addOptions() {
        this.list.setEmotes(EmoteHolder.list, false);
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        if (this.list != null) linearLayout.addChild(this.list.createBackButton());

        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .build()
        );
        linearLayout.addChild(Button.builder(FullMenuScreen.CONFIG, button -> this.minecraft.setScreen(new EmoteMenu(this)))
                .build()
        );
    }

    @Override
    protected void onPressed(EmoteListWidget.ListEntry selected) {
        if (selected instanceof EmoteListWidget.EmoteEntry entry &&
                entry.getEmote().playEmote() &&
                this.lastScreen instanceof FastMenuScreen fast
        ) {
            this.lastScreen = fast.parent;
        }
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        if (this.minecraft.player instanceof IPlayerEntity entity &&
                EmotePlayer.isRunningEmote(entity.emotecraft$getEmote())
        ) {
            return;
        }

        super.renderBlurredBackground(guiGraphics);
    }

    @Override
    protected void repositionElements() {
        addOptions();
        super.repositionElements();
        this.layout.arrangeElements();
    }

    @Override
    public void tick() {
        if (this.preview != null && this.list.getSelected() == this.list.getHovered()) {
            this.preview.getMannequin().stopEmote();
        }
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
