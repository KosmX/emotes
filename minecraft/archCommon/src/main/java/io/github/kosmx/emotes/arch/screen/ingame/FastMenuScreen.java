package io.github.kosmx.emotes.arch.screen.ingame;

import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FastMenuScreen extends Screen implements FastChooseController {
    protected static final Component TITLE = Component.translatable("emotecraft.fastmenu");

    private static final Component WARN_NO_EMOTECRAFT = Component.translatable("emotecraft.no_server");
    private static final Component WARN_ONLY_PROXY = Component.translatable("emotecraft.only_proxy");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected final Screen parent;

    protected AbstractFastChooseWidget fastMenu;

    public FastMenuScreen(Screen parent) {
        super(FastMenuScreen.TITLE);
        this.parent = parent;
    }

    @Override
    public void init() {
        if (ClientPacketManager.isRemoteAvailable()) {
            // this.layout.addTitleHeader(getTitle(), this.font); TODO Do we want this?
            this.layout.setHeaderHeight(0);
        } else if (ClientPacketManager.isAvailableProxy()) {
            this.layout.addTitleHeader(FastMenuScreen.WARN_ONLY_PROXY, this.font);
        } else {
            this.layout.addTitleHeader(FastMenuScreen.WARN_NO_EMOTECRAFT, this.font);
        }

        this.fastMenu = this.layout.addToContents(new PreviewFastChooseWidget(this, true, 0, 0, 512),
                LayoutSettings::alignVerticallyMiddle
        );

        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(FullMenuScreen.TITLE, button -> this.minecraft.setScreen(new FullMenuScreen(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.fastMenu != null) {
            this.fastMenu.setSize(Math.min(Math.round(Math.min(this.width * 0.8F, (this.height - this.layout.getHeaderHeight()) * 0.8F)), 512));
        }
        this.layout.arrangeElements();
    }

    @Override
    public void removed() {
        super.removed();
        if (this.fastMenu != null) this.fastMenu.removed();
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        // no-op
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        if (EmotecraftClientMod.OPEN_MENU_KEY.matches(keyEvent)) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (super.mouseClicked(event, bl)) {
            return true;
        }
        if (EmotecraftClientMod.OPEN_MENU_KEY.matchesMouse(event)) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fastMenu != null) {
            this.fastMenu.tick();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean doHoverPart(IChooseElement part) {
        return part.hasEmote();
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo info) {
        return info.button() == 0;
    }

    @Override
    public boolean onClick(IChooseElement element, MouseButtonEvent event, boolean unused) {
        if(element.getEmote() != null){
            boolean bl = element.getEmote().playEmote();
            if (bl) Minecraft.getInstance().setScreen(null);
            return bl;
        }
        return false;
    }

    @Override
    public boolean doesShowInvalid() {
        return false;
    }
}
