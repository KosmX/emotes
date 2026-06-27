package io.github.kosmx.emotes.arch.screen.ingame;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.config.CloseWheel;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class FastMenuScreen extends Screen implements FastChooseController {
    protected static final Component TITLE = Component.translatable("emotecraft.fastmenu");

    private static final Component WARN_NO_SERVER = Component.translatable("emotecraft.no_server").withColor(CommonColors.SOFT_RED);
    public static final Component WARN_DIFFERENT_SERVER = Component.translatable("emotecraft.different_server").withColor(CommonColors.SOFT_RED);
    private static final Component WARN_ONLY_PROXY = Component.translatable("emotecraft.only_proxy");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 0, HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT);
    protected final Screen parent;

    protected AbstractFastChooseWidget fastMenu;

    public FastMenuScreen(Screen parent) {
        super(FastMenuScreen.TITLE);
        this.parent = parent;
    }

    @Override
    public void init() {
        this.layout.setHeaderHeight(this.font.lineHeight * 2);
        if (ClientNetwork.INSTANCE.isActive()) {
            if (ClientPacketManager.isInstanceOutdatedForStreaming(ClientNetwork.INSTANCE)) {
                MultiLineTextWidget widget = this.layout.addToHeader(new MultiLineTextWidget(FastMenuScreen.WARN_DIFFERENT_SERVER, this.font)
                        .setCentered(true).setMaxRows(3).setMaxWidth(Mth.ceil(this.width / 1.2))
                );
                this.layout.setHeaderHeight(widget.getHeight());
            } else {
                this.layout.setHeaderHeight(0);
            }
        } else if (ClientPacketManager.isAvailableProxy()) {
            this.layout.addTitleHeader(FastMenuScreen.WARN_ONLY_PROXY, this.font);
        } else {
            this.layout.addTitleHeader(FastMenuScreen.WARN_NO_SERVER, this.font);
        }

        this.fastMenu = this.layout.addToContents(new PreviewFastChooseWidget(this, true, 0, 0, 512),
                LayoutSettings::alignVerticallyMiddle
        );

        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(FullMenuScreen.TITLE, button -> this.minecraft.gui.setScreen(new FullMenuScreen(this)))
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
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor graphics) {
        // no-op
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        if (supportsKeyboardNavigation()) {
            List<IChooseElement> chooseElements = this.fastMenu.getChooseElements();
            int digit = keyEvent.getDigit() - 1;
            if (digit >= 0 && digit < chooseElements.size() && onClick(chooseElements.get(digit), keyEvent, false)) {
                return true;
            }
        }
        if (PlatformTools.getConfig().closeWheelType.get() == CloseWheel.PRESS && EmotecraftClientMod.OPEN_MENU_KEY.matches(keyEvent)) {
            return onToggleKey(keyEvent);
        }
        if (super.keyPressed(keyEvent)) {
            if (supportsKeyboardNavigation() && (keyEvent.isRight() || keyEvent.isLeft())) this.fastMenu.keyPressed(keyEvent); // Force
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent keyEvent) {
        if (PlatformTools.getConfig().closeWheelType.get() == CloseWheel.HOLD && EmotecraftClientMod.OPEN_MENU_KEY.matches(keyEvent)) {
            return onToggleKey(keyEvent);
        }
        return super.keyReleased(keyEvent);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        if (PlatformTools.getConfig().closeWheelType.get() == CloseWheel.PRESS && EmotecraftClientMod.OPEN_MENU_KEY.matchesMouse(event)) {
            return onToggleKey(event);
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (PlatformTools.getConfig().closeWheelType.get() == CloseWheel.HOLD && EmotecraftClientMod.OPEN_MENU_KEY.matchesMouse(event)) {
            return onToggleKey(event);
        }
        return super.mouseReleased(event);
    }

    @SuppressWarnings("unused")
    protected boolean onToggleKey(InputWithModifiers event) {
        onClose();
        return true;
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
        this.minecraft.gui.setScreen(this.parent);
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
    public boolean onClick(IChooseElement element, InputWithModifiers event, boolean unused) {
        if(element.getEmote() != null){
            boolean bl = element.getEmote().playEmote();
            if (bl) Minecraft.getInstance().gui.setScreen(null);
            return bl;
        }
        return false;
    }

    @Override
    public boolean doesShowInvalid() {
        return false;
    }

    @Override
    public boolean supportsKeyboardNavigation() {
        return PlatformTools.getConfig().enableWheelKeyboardNav.get();
    }
}
