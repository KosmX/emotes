package io.github.kosmx.emotes.arch.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.screen.ConfigScreen;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.library.LibraryModals;
import io.github.kosmx.emotes.arch.screen.components.EmoteSubScreen;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.FastChooseController;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.config.Serializer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class EmoteMenu extends EmoteSubScreen implements FastChooseController {
    private static final Component TITLE = Component.translatable("emotecraft.menu");

    public static final Component OPEN_FOLDER = Component.translatable("emotecraft.openFolder");
    private static final Component OPTIONS = Component.translatable("emotecraft.options.options");

    public static final Component RESET = Component.translatable("controls.reset");

    private static final Component KEYBIND = Component.translatable("emotecraft.options.keybind");
    private static final Component FASTMENU = Component.translatable("emotecraft.options.fastmenu")
            .append(CommonComponents.SPACE)
            .append(Component.translatable("emotecraft.options.fastmenu2"))
            .append(CommonComponents.SPACE)
            .append(Component.translatable("emotecraft.options.fastmenu3"));

    private static final Component SURE = Component.translatable("emotecraft.sure");
    private static final Component SURE2 = Component.translatable("emotecraft.sure2");

    private static final Component RESET_ONE = Component.translatable("controls.reset");
    private static final Component RESET_ALL = Component.translatable("controls.resetAll");

    private static final Component RESET_ALL_TITLE = Component.translatable("emotecraft.resetAllKeys.title");
    private static final Component RESET_ALL_MSG = Component.translatable("emotecraft.resetAllKeys.message");

    public long activeKeyTime;
    private Button setKeyButton;
    private Button resetButton;
    private boolean resetOnlySelected;

    protected AbstractFastChooseWidget fastChoose;

    public EmoteMenu(Screen parent) {
        super(EmoteMenu.TITLE, true, parent);
    }

    @Override
    protected void addContents() {
        LinearLayout linearLayout = this.layout.addToContents(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        if (this.list == null) {
            this.list = newEmoteListWidget();
            this.list.setCompactMode(true);
            addOptions();
        }
        linearLayout.addChild(this.list, LayoutSettings::alignVerticallyBottom);

        GridLayout gridLayout = linearLayout.addChild(new GridLayout());
        gridLayout.defaultCellSetting().padding(4, Button.DEFAULT_SPACING / 3, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        rowHelper.addChild(new MultiLineTextWidget(KEYBIND, this.font).setMaxWidth(
                Button.SMALL_WIDTH * 2
        ), 2);

        this.setKeyButton = rowHelper.addChild(Button.builder(InputConstants.UNKNOWN.getDisplayName(), button -> {
            if (this.list != null && this.list.getSelected() != null){
                this.activeKeyTime = 200;
            }
        }).width(Button.SMALL_WIDTH).build());
        this.setKeyButton.active = false;

        this.resetButton = rowHelper.addChild(Button.builder(RESET, this::resetKeyAction)
                .width(Button.SMALL_WIDTH)
                .build()
        );
        this.resetButton.active = false;

        rowHelper.addChild(new MultiLineTextWidget(FASTMENU, this.font).setMaxWidth(
                Button.SMALL_WIDTH * 2
        ), 2, gridLayout.newCellSettings().paddingTop(Button.DEFAULT_SPACING));

        this.fastChoose = rowHelper.addChild(new PreviewFastChooseWidget(this, false, 0, 0, 256), 2,
                rowHelper.newCellSettings().alignHorizontallyCenter().paddingTop(Button.DEFAULT_SPACING / 2)
        );
    }

    @Override
    protected void addOptions() {
        if (this.list != null) this.list.setEmotes(EmoteHolder.list, true);
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        if (this.list != null) linearLayout.addChild(this.list.createBackButton());

        linearLayout.addChild(Button.builder(EmoteMenu.OPEN_FOLDER, button -> PlatformTools.openExternalEmotesDir())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(EmoteMenu.OPTIONS, button -> this.minecraft.gui.setScreen(new ConfigScreen(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

    private void resetKeyAction(Button button){
        if (resetOnlySelected) {
            if (this.list == null || !(this.list.getFocused() instanceof EmoteListWidget.EmoteLikeEntry entry)) return;
            unbind(entry.getUuid());
            onPressed(this.list.getSelected());
        } else {
            this.minecraft.gui.setScreen(new ConfirmScreen(aBoolean -> {
                if (aBoolean) {
                    PlatformTools.getConfig().keyBinds.clear(); //reset :D
                    onPressed(this.list.getSelected());
                }
                this.minecraft.gui.setScreen(EmoteMenu.this);
                }, RESET_ALL_TITLE, RESET_ALL_MSG.copy().append(" (" + PlatformTools.getConfig().keyBinds.size() + ")")
            ));
        }
    }

    @Override
    protected void repositionElements() {
        if (this.fastChoose != null) {
            this.fastChoose.setSize(Math.min(Math.round(Math.min(this.width / 2.5F, this.height / 2.3F)), 256));
        }
        super.repositionElements();
    }

    @Override
    protected void onPressed(EmoteListWidget.ListEntry selected) {
        if (this.resetButton == null) return;

        this.setKeyButton.active = this.resetButton.active = selected instanceof EmoteListWidget.EmoteLikeEntry;

        if (selected instanceof EmoteListWidget.EmoteLikeEntry entry) {
            this.setKeyButton.setMessage(getKey(entry.getUuid()).getDisplayName());
            this.resetOnlySelected = isBound(entry.getUuid());
        } else {
            this.resetOnlySelected = false;
        }

        if (resetOnlySelected) {
            this.resetButton.active = true;
            this.resetButton.setMessage(RESET_ONE);
        } else {
            if (!PlatformTools.getConfig().keyBinds.isEmpty()) {
                this.resetButton.active = true;
                this.resetButton.setMessage(RESET_ALL.copy().append(" (" + PlatformTools.getConfig().keyBinds.size() + ")"));
            } else {
                this.resetButton.active = false;
                this.resetButton.setMessage(RESET_ONE);
            }
        }
    }

    @Override
    public void tick(){
        if(activeKeyTime == 1){
            setFocused(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
        super.tick();
        if (this.fastChoose != null) {
            this.fastChoose.tick();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (this.activeKeyTime != 0 && this.list != null && this.list.getFocused() != null) {
            return setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
        }
        return super.mouseClicked(event, bl);
    }

    private boolean setKey(InputConstants.Key key){
        if (this.list == null || !(this.list.getFocused() instanceof EmoteListWidget.EmoteLikeEntry entry)) return false;
        // Resolve the emote itself (local instantly, library fetched once), then bind it — the binding stores the emote, not a UUID.
        entry.getEmote().whenCompleteAsync((animation, th) -> {
            if (th != null) {
                LibraryModals.show(th);
                return;
            }
            EmoteHolder holder = new EmoteHolder(animation);
            if (!applyKey(false, holder, key)) {
                this.minecraft.gui.setScreen(new ConfirmScreen(choice -> {
                    if (choice) applyKey(true, holder, key);
                    this.minecraft.gui.setScreen(this);
                }, SURE, SURE2));
            }
        }, this.minecraft);
        return true;
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputConstants.Key key){
        Map<InputConstants.Key, EmoteHolder> keyBinds = PlatformTools.getConfig().keyBinds;

        EmoteHolder current = key.equals(InputConstants.UNKNOWN) ? null : keyBinds.get(key);
        if (current != null && !current.getUuid().equals(emote.getUuid()) && !force) {
            return false; // key already taken by another emote — caller asks to confirm the override
        }

        unbind(emote.getUuid());                 // one key per emote: drop its previous bind
        if (!key.equals(InputConstants.UNKNOWN)) {
            keyBinds.put(key, emote);            // one emote per key: overwrites any conflicting bind
        }
        onPressed(this.list.getSelected());
        this.activeKeyTime = 0;
        return true;
    }

    /** @return the key {@code emoteId} is bound to, matching by emote id against the stored holders, or UNKNOWN. */
    @NotNull
    public static InputConstants.Key getKey(UUID emoteId) {
        for (Map.Entry<InputConstants.Key, EmoteHolder> entry : PlatformTools.getConfig().keyBinds.entrySet()) {
            if (entry.getValue().getUuid().equals(emoteId)) return entry.getKey();
        }
        return InputConstants.UNKNOWN;
    }

    private static boolean isBound(UUID emoteId) {
        return getKey(emoteId) != InputConstants.UNKNOWN;
    }

    private static void unbind(UUID emoteId) {
        PlatformTools.getConfig().keyBinds.values().removeIf(holder -> holder.getUuid().equals(emoteId));
    }

    @Override
    public void removed() {
        super.removed();
        Serializer.INSTANCE.saveConfig();
        if (this.fastChoose != null) this.fastChoose.removed();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.list != null && this.list.getFocused() != null && activeKeyTime != 0) {
            if (keyEvent.isEscape()) {
                return setKey(InputConstants.UNKNOWN);
            } else {
                return setKey(InputConstants.getKey(keyEvent));
            }
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo info) {
        return (info.button() == 0 || info.button() == 1) && activeKeyTime == 0;
    }

    @Override
    public boolean onClick(IChooseElement element, InputWithModifiers event, boolean bl) {
        if (activeKeyTime != 0) return false;
        if (event.input() == 1) {
            element.clearEmote();
            return true;
        } else if (list != null && list.getFocused() instanceof EmoteListWidget.EmoteLikeEntry entry) {
            element.setEmote(entry);
            return true;
        } else{
            return false;
        }
    }

    @Override
    public boolean doHoverPart(IChooseElement part){
        return activeKeyTime == 0;
    }

    @Override
    public boolean doesShowInvalid() {
        return true;
    }

    @Override
    public boolean supportsKeyboardNavigation() {
        return false;
    }
}
