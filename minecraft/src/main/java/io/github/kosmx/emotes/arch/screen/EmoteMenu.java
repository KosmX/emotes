package io.github.kosmx.emotes.arch.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.screen.ConfigScreen;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.library.LibraryModals;
import io.github.kosmx.emotes.arch.screen.components.EmoteSubScreen;
import io.github.kosmx.emotes.arch.screen.utils.KeyBindUtils;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class EmoteMenu extends EmoteSubScreen implements FastChooseController {
    private static final Component TITLE = Component.translatable("emotecraft.menu");

    public static final Component OPEN_FOLDER = Component.translatable("emotecraft.openFolder");
    private static final Component OPTIONS = Component.translatable("emotecraft.options.options");

    public static final Component RESET = Component.translatable("controls.reset");
    private static final Component KEYBINDS = Component.translatable("controls.keybinds");

    private static final Component KEYBIND = Component.translatable("emotecraft.options.keybind");
    private static final Component FASTMENU = Component.translatable("emotecraft.options.fastmenu")
            .append(CommonComponents.SPACE)
            .append(Component.translatable("emotecraft.options.fastmenu2"))
            .append(CommonComponents.SPACE)
            .append(Component.translatable("emotecraft.options.fastmenu3"));

    private long activeKeyTime;
    private Button setKeyButton;
    private Button keyBindsButton;

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

        this.setKeyButton = rowHelper.addChild(Button.builder(InputConstants.UNKNOWN.getDisplayName(), _ -> {
            if (this.list != null && this.list.getSelected() != null) {
                setActiveKeyTime(200);
            }
        }).width(Button.SMALL_WIDTH).build());
        this.setKeyButton.active = false;

        this.keyBindsButton = rowHelper.addChild(Button.builder(KEYBINDS, _ -> this.minecraft.gui.setScreen(new KeyBindsMenu(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );

        rowHelper.addChild(new MultiLineTextWidget(FASTMENU, this.font).setMaxWidth(
                Button.SMALL_WIDTH * 2
        ), 2, gridLayout.newCellSettings().paddingTop(Button.DEFAULT_SPACING));

        this.fastChoose = rowHelper.addChild(new PreviewFastChooseWidget(this, false, 0, 0, 256), 2,
                rowHelper.newCellSettings().alignHorizontallyCenter().paddingTop(Button.DEFAULT_SPACING / 2)
        );

        if (this.list != null) onPressed(this.list.getSelected()); // Initial state of the buttons
    }

    @Override
    public void added() {
        super.added();
        if (this.list != null) onPressed(this.list.getSelected()); // Binds may have changed on the key binds screen, init is not called again
    }

    @Override
    protected void addOptions() {
        if (this.list != null) this.list.setEmotes(EmoteHolder.list, true);
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        if (this.list != null) linearLayout.addChild(this.list.createBackButton());

        linearLayout.addChild(Button.builder(EmoteMenu.OPEN_FOLDER, _ -> PlatformTools.openExternalEmotesDir())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        linearLayout.addChild(Button.builder(EmoteMenu.OPTIONS, _ -> this.minecraft.gui.setScreen(new ConfigScreen(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );
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
        if (this.keyBindsButton == null) return;

        this.setKeyButton.active = selected instanceof EmoteListWidget.EmoteLikeEntry;
        if (selected instanceof EmoteListWidget.EmoteLikeEntry entry) {
            this.setKeyButton.setMessage(KeyBindUtils.getKeyMessage(KeyBindUtils.getKey(entry.getUuid()), this.activeKeyTime != 0));
        }

        this.keyBindsButton.active = !PlatformTools.getConfig().keyBinds.isEmpty();
    }

    /** Starts or stops waiting for a key, and shows that on the key button. */
    private void setActiveKeyTime(long activeKeyTime) {
        this.activeKeyTime = activeKeyTime;
        if (this.list != null) onPressed(this.list.getSelected());
    }

    @Override
    public void tick(){
        if (this.activeKeyTime == 1) {
            setActiveKeyTime(0); // Waiting for a key timed out
        } else if (this.activeKeyTime != 0) {
            this.activeKeyTime--;
        }
        super.tick();
        if (this.fastChoose != null) {
            this.fastChoose.tick();
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        if (this.activeKeyTime != 0 && setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()))) {
            return true;
        }
        return super.mouseClicked(event, bl);
    }

    private boolean setKey(InputConstants.Key key) {
        if (this.list == null || !(this.list.getSelected() instanceof EmoteListWidget.EmoteLikeEntry entry)) return false;
        setActiveKeyTime(0);
        // Resolve the emote itself (local instantly, library fetched once), then bind it — the binding stores the emote, not a UUID.
        entry.getEmote().whenCompleteAsync((animation, th) -> {
            if (th != null) {
                LibraryModals.show(th);
                return;
            }
            KeyBindUtils.bind(this, new EmoteHolder(animation), key, () -> {
                if (this.list != null) onPressed(this.list.getSelected());
            });
        }, this.minecraft);
        return true;
    }

    @Override
    public void removed() {
        super.removed();
        Serializer.INSTANCE.saveConfig();
        if (this.fastChoose != null) this.fastChoose.removed();
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent) {
        if (this.list != null && this.list.getSelected() != null && this.activeKeyTime != 0) {
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
        return (info.button() == InputConstants.MOUSE_BUTTON_LEFT || info.button() == InputConstants.MOUSE_BUTTON_RIGHT) && activeKeyTime == 0;
    }

    @Override
    public boolean onClick(IChooseElement element, InputWithModifiers event, boolean bl) {
        if (this.activeKeyTime != 0) return false;
        if (event.input() == InputConstants.MOUSE_BUTTON_RIGHT) {
            element.clearEmote();
            return true;
        } else if (this.list != null && this.list.getSelected() instanceof EmoteListWidget.EmoteLikeEntry entry) {
            element.setEmote(entry);
            return true;
        } else{
            return false;
        }
    }

    @Override
    public boolean doHoverPart(IChooseElement part){
        return this.activeKeyTime == 0;
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
