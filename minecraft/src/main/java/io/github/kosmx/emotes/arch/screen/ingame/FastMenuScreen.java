package io.github.kosmx.emotes.arch.screen.ingame;

import com.mojang.blaze3d.platform.InputConstants;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.arch.gui.dropdown.AppDropdownMenu;
import io.github.kosmx.emotes.arch.gui.dropdown.DropdownLayer;
import io.github.kosmx.emotes.arch.gui.widgets.GuideWidget;
import io.github.kosmx.emotes.arch.gui.widgets.ItemActions;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import io.github.kosmx.emotes.arch.online.OnlineNetworkInstance;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.CloseWheel;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class FastMenuScreen extends Screen implements FastChooseController {
    protected static final Component TITLE = Component.translatable("emotecraft.fastmenu");

    private static final Component WARN_NO_SERVER = Component.translatable("emotecraft.no_server").withColor(CommonColors.SOFT_RED);
    public static final Component WARN_DIFFERENT_SERVER = Component.translatable("emotecraft.different_server").withColor(CommonColors.SOFT_RED);
    private static final Component WARN_ONLY_PROXY = Component.translatable("emotecraft.only_proxy");
    private static final Component WARN_ONLY_ONLINE = Component.translatable("emotecraft.only_online");
    private static final Component RECONNECT_ONLINE = Component.translatable("emotecraft.online.reconnect");
    private static final Component ACTIONS_GUIDE = Component.translatable("emotecraft.guide.wheel");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 0, HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT);
    private final FrameLayout contents = this.layout.addToContents(new FrameLayout());
    private final DropdownLayer dropdowns = new DropdownLayer();
    protected final Screen parent;

    protected AbstractFastChooseWidget fastMenu;
    private Button reconnectOnline;
    private GuideWidget guide;
    @Nullable
    private AppDropdownMenu menu;

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
        } else if (OnlineNetworkInstance.INSTANCE.isActive()) {
            this.layout.addTitleHeader(FastMenuScreen.WARN_ONLY_ONLINE, this.font);
        } else if (ClientPacketManager.isAvailableProxy()) {
            this.layout.addTitleHeader(FastMenuScreen.WARN_ONLY_PROXY, this.font);
        } else {
            this.layout.addTitleHeader(FastMenuScreen.WARN_NO_SERVER, this.font);
        }

        this.fastMenu = this.contents.addChild(new PreviewFastChooseWidget(this, this.dropdowns, true, 0, 0, 512));

        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));
        if (PlatformTools.getConfig().onlineDebug.get()) {
            this.reconnectOnline = linearLayout.addChild(Button.builder(RECONNECT_ONLINE, _ -> OnlineEmotes.connect())
                    .width(Button.SMALL_WIDTH)
                    .build()
            );
            this.reconnectOnline.active = canReconnectOnline(); // and kept up to date on every tick
        }
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(FullMenuScreen.TITLE, button -> this.minecraft.gui.setScreen(new FullMenuScreen(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.guide = addRenderableWidget(new GuideWidget(ACTIONS_GUIDE, this.font, Button.SMALL_WIDTH * 2)); // Not in the layout, it is placed right of the wheel once that is arranged
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        int contentHeight = this.layout.getContentHeight();
        if (this.fastMenu != null) {
            this.fastMenu.setSize(Math.min(Math.round(Math.min(this.width * 0.8F, contentHeight * 0.8F)), 512));
        }
        this.contents.setMinHeight(contentHeight); // Fills the area between header and footer, so the frame centers the wheel in it
        this.layout.arrangeElements();
        if (this.guide != null && this.fastMenu != null) {
            this.guide.centerIn(this.fastMenu.getRight(), this.layout.getHeaderHeight(), this.width, this.layout.getHeaderHeight() + contentHeight);
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        this.dropdowns.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    protected void updateNarratedWidget(@NonNull NarrationElementOutput output) {
        if (!this.dropdowns.updateNarration(output)) super.updateNarratedWidget(output);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.dropdowns.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
        if (this.dropdowns.keyPressed(keyEvent)) {
            return true;
        }
        if (supportsKeyboardNavigation()) {
            OptionalInt intKey = InputConstants.getKey(keyEvent).getNumericKeyValue();
            if (intKey.isPresent()) {
                List<IChooseElement> chooseElements = this.fastMenu.getChooseElements();
                int digit = intKey.getAsInt() - 1;
                if (digit >= 0 && digit < chooseElements.size() && onClick(chooseElements.get(digit), keyEvent, false)) {
                    return true;
                }
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
        if (this.dropdowns.mouseClicked(event, bl)) {
            return true;
        }
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
        if (this.reconnectOnline != null) {
            this.reconnectOnline.active = canReconnectOnline();
        }
    }

    private static boolean canReconnectOnline() {
        return !OnlineNetworkInstance.INSTANCE.isActive() && OnlineEmotes.isWorldShared();
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
        return info.button() == InputConstants.MOUSE_BUTTON_LEFT || info.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean onClick(IChooseElement element, InputWithModifiers event, boolean unused) {
        EmoteHolder emote = element.getEmote();
        if (emote == null) return false;
        if (event.input() == InputConstants.MOUSE_BUTTON_RIGHT) return openMenu(element, emote);
        return play(emote.emote);
    }

    private boolean play(Animation emote) {
        boolean played = EmoteHolder.playEmote(ClientUtil.getClientPlayer(), emote);
        if (played) this.minecraft.gui.setScreen(null);
        return played;
    }

    private boolean openMenu(IChooseElement element, EmoteHolder emote) {
        // The bound copy keeps no file name, and registering its icon would replace the loaded one's, which has the same id
        EmoteHolder loaded = Objects.requireNonNullElse(EmoteHolder.getEmoteFromUuid(emote.getUuid()), emote);
        Runnable playWithoutSound = EmoteHolder.hasSound(emote.emote) ? () -> play(EmoteHolder.withoutSound(emote.emote)) : null;
        URI page = loaded.page();
        Path file = loaded.file();
        if (playWithoutSound == null && page == null && file == null) return false;

        this.menu = new AppDropdownMenu(this.dropdowns, element::getRectangle, this::closeMenu, scope ->
                new ItemActions(scope, emote.name, emote.author, this::closeMenu).addEmote(playWithoutSound, page, () -> loaded.icon(element.getRectangle()), file)
        );
        this.menu.setExpanded(true);
        return true;
    }

    private void closeMenu() {
        if (this.menu != null) this.menu.setExpanded(false);
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
