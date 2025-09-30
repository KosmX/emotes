package io.github.kosmx.emotes.arch.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.screen.ConfigScreen;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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

        this.list = linearLayout.addChild(newEmoteListWidget(), LayoutSettings::alignVerticallyBottom);
        this.list.setCompactMode(true);
        addOptions();

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
        linearLayout.addChild(Button.builder(EmoteMenu.OPTIONS, button -> this.minecraft.setScreen(new ConfigScreen(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

    private void resetKeyAction(Button button){
        if(resetOnlySelected) {
            if (this.list == null || this.list.getFocused() == null) return;
            PlatformTools.getConfig().emoteKeyMap.removeL(this.list.getFocusedEmote().getUuid());
            onPressed(this.list.getSelected());
        } else {
            this.minecraft.setScreen(new ConfirmScreen(aBoolean -> {
                if (aBoolean) {
                    PlatformTools.getConfig().emoteKeyMap.clear(); //reset :D
                    onPressed(this.list.getSelected());
                }
                this.minecraft.setScreen(EmoteMenu.this);
                }, RESET_ALL_TITLE, RESET_ALL_MSG.copy().append(" (" + PlatformTools.getConfig().emoteKeyMap.size() + ")")
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

        this.setKeyButton.active = this.resetButton.active = selected instanceof EmoteListWidget.EmoteEntry;

        if (selected instanceof EmoteListWidget.EmoteEntry entry) {
            this.setKeyButton.setMessage(getKey(entry.getEmote().getUuid()).getDisplayName());
            this.resetOnlySelected = PlatformTools.getConfig().emoteKeyMap.containsL(entry.getEmote().getUuid());
        } else {
            this.resetOnlySelected = false;
        }

        if (resetOnlySelected) {
            this.resetButton.active = true;
            this.resetButton.setMessage(RESET_ONE);
        } else {
            if (!PlatformTools.getConfig().emoteKeyMap.isEmpty()) {
                this.resetButton.active = true;
                this.resetButton.setMessage(RESET_ALL.copy().append(" (" + PlatformTools.getConfig().emoteKeyMap.size() + ")"));
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
        boolean bl = false;
        if (this.list != null && this.list.getFocused() != null) {
            bl = true;
            if (!applyKey(false, this.list.getFocusedEmote(), key)) {
                this.minecraft.setScreen(new ConfirmScreen(choice -> {
                    if (choice) {
                        applyKey(true, this.list.getFocusedEmote(), key);
                    }
                    this.minecraft.setScreen(this);
                }, SURE, SURE2));
            }
        }
        return bl;
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputConstants.Key key){
        boolean bl = true;
        for(EmoteHolder emoteHolder : EmoteHolder.list){
            if(! key.equals(InputConstants.UNKNOWN) && getKey(emoteHolder.getUuid()).equals(key)){
                bl = false;
                if(force){
                    //emoteHolder.keyBinding = TmpGetters.getDefaults().getUnknownKey();
                    PlatformTools.getConfig().emoteKeyMap.removeL(emoteHolder.getUuid());
                }
            }
        }
        if(bl || force){
            PlatformTools.getConfig().emoteKeyMap.put(emote.getUuid(), key);
            onPressed(this.list.getSelected());
        }
        this.activeKeyTime = 0;
        return bl;
    }

    @NotNull
    public static InputConstants.Key getKey(UUID emoteID) {
        InputConstants.Key key;
        if((key = PlatformTools.getConfig().emoteKeyMap.getR(emoteID)) == null){
            return InputConstants.UNKNOWN;
        }
        return key;
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
    public boolean onClick(IChooseElement element, MouseButtonEvent event, boolean bl) {
        if (activeKeyTime != 0) return false;
        if (event.button() == 1) {
            element.clearEmote();
            return true;
        } else if (list != null && list.getFocused() != null) {
            element.setEmote(list.getFocusedEmote());
            return true;
        }else{
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
}
