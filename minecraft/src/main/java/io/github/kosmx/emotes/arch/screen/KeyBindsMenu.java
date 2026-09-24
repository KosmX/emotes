package io.github.kosmx.emotes.arch.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.components.PreviewScreen;
import io.github.kosmx.emotes.arch.screen.utils.KeyBindUtils;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.config.Serializer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lists every emote key bind with a preview of the hovered emote, and lets you change or reset one, or reset all of them.
 */
public class KeyBindsMenu extends PreviewScreen {
    private static final Component TITLE = Component.translatable("controls.keybinds.title");
    private static final Component RESET_ALL = Component.translatable("controls.resetAll");

    private static final Component RESET_ALL_TITLE = Component.translatable("emotecraft.resetAllKeys.title");
    private static final Component RESET_ALL_MSG = Component.translatable("emotecraft.resetAllKeys.message");

    private static final int ITEM_HEIGHT = 24;
    private static final int ICON_SIZE = 20;
    private static final int PADDING = 4;

    private BindList list;
    private Button resetAllButton;
    @Nullable
    private BindEntry selected; // entry waiting for its new key

    public KeyBindsMenu(Screen lastScreen) {
        super(TITLE, lastScreen);
    }

    @Override
    protected void addTitle() {
        this.layout.addTitleHeader(getTitle(), this.font);
    }

    @Override
    protected void addContents() {
        this.list = this.layout.addToContents(new BindList());
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        this.resetAllButton = footer.addChild(Button.builder(RESET_ALL, _ -> resetAll())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        refreshResetAll();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.list.updateSize(this.width, this.layout);
        repositionPreview(this.list.getRowLeft());
    }

    private void select(@Nullable BindEntry entry) {
        this.selected = entry;
        this.list.children().forEach(BindEntry::refresh);
    }

    private void bind(BindEntry entry, InputConstants.Key key) {
        select(null);
        KeyBindUtils.bind(this, entry.holder, key, this::reload);
    }

    /** Shows the binds from the config again, the screen is not initialized again when coming back to it. */
    private void reload() {
        this.list.reload();
        refreshResetAll();
    }

    private void unbind(BindEntry entry) {
        KeyBindUtils.unbind(entry.holder.getUuid());
        this.list.removeEntry(entry);
        refreshResetAll();
    }

    private void resetAll() {
        int count = PlatformTools.getConfig().keyBinds.size();
        this.minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                PlatformTools.getConfig().keyBinds.clear();
                reload();
            }
            this.minecraft.gui.setScreen(this);
        }, RESET_ALL_TITLE, RESET_ALL_MSG.copy().append(" (" + count + ")")));
    }

    private void refreshResetAll() {
        int count = PlatformTools.getConfig().keyBinds.size();
        this.resetAllButton.active = count > 0;
        this.resetAllButton.setMessage(count > 0 ? RESET_ALL.copy().append(" (" + count + ")") : RESET_ALL);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (this.selected != null) {
            bind(this.selected, InputConstants.Type.MOUSE.getOrCreate(event.button()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.selected != null) {
            bind(this.selected, event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void tick() {
        if (this.list.getHovered() instanceof BindEntry entry) {
            previewEmote(entry, () -> CompletableFuture.completedFuture(entry.holder.emote));
        } else {
            clearPreviewed(false);
        }
        super.tick();
    }

    @Override
    public void removed() {
        super.removed();
        Serializer.INSTANCE.saveConfig();
    }

    private final class BindList extends ContainerObjectSelectionList<BindEntry> {
        private BindList() {
            super(KeyBindsMenu.this.minecraft, KeyBindsMenu.this.width, KeyBindsMenu.this.layout.getContentHeight(),
                    KeyBindsMenu.this.layout.getHeaderHeight(), ITEM_HEIGHT
            );
            reload();
        }

        private void reload() {
            replaceEntries(PlatformTools.getConfig().keyBinds.entrySet().stream()
                    .sorted(Comparator.comparing((Map.Entry<InputConstants.Key, EmoteHolder> bind) -> bind.getValue().name.getString()))
                    .map(bind -> new BindEntry(bind.getKey(), bind.getValue()))
                    .toList()
            );
            refreshScrollAmount();
        }

        @Override
        public int getRowWidth() {
            return this.width / 2;
        }

        @Override
        public @Nullable BindEntry getHovered() {
            return super.getHovered();
        }

        @Override
        public void removeEntry(@NonNull BindEntry entry) {
            super.removeEntry(entry);
        }
    }

    private final class BindEntry extends ContainerObjectSelectionList.Entry<BindEntry> {
        private final InputConstants.Key key;
        private final EmoteHolder holder;
        private final Button changeButton;
        private final Button resetButton;

        private BindEntry(InputConstants.Key key, EmoteHolder holder) {
            this.key = key;
            this.holder = holder;
            this.changeButton = Button.builder(key.getDisplayName(), _ -> select(this))
                    .width(75)
                    .createNarration(message -> Component.translatable("narrator.controls.bound", holder.name, message.get()))
                    .build();
            this.resetButton = Button.builder(EmoteMenu.RESET, _ -> unbind(this))
                    .width(50)
                    .createNarration(_ -> Component.translatable("narrator.controls.reset", holder.name))
                    .build();
            refresh();
        }

        private void refresh() {
            this.changeButton.setMessage(KeyBindUtils.getKeyMessage(this.key, selected == this));
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            if (hovered) {
                graphics.fill(getContentX() - 1, getContentY() - 1, getContentRight() + 1, getContentBottom() + 1, ARGB.color(128, 66, 66, 66));
            }

            int buttonY = getContentYMiddle() - Button.DEFAULT_HEIGHT / 2;
            this.resetButton.setPosition(getContentRight() - this.resetButton.getWidth(), buttonY);
            this.resetButton.extractRenderState(graphics, mouseX, mouseY, a);
            this.changeButton.setPosition(this.resetButton.getX() - PADDING - this.changeButton.getWidth(), buttonY);
            this.changeButton.extractRenderState(graphics, mouseX, mouseY, a);

            Identifier icon = getIcon();
            if (icon != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, icon, getContentX(), getContentYMiddle() - ICON_SIZE / 2, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, 256, 256, 256, 256);
            }

            int nameX = getContentX() + ICON_SIZE + PADDING; // Always offset, so the names line up
            int textY = getContentYMiddle() - font.lineHeight / 2;
            graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE).acceptScrolling(this.holder.name,
                    nameX, nameX, this.changeButton.getX() - PADDING * 2, textY, textY + font.lineHeight
            );
        }

        /** Prefers the loaded copy of the emote: it already owns the icon texture, the bound holder would register another one. */
        private @Nullable Identifier getIcon() {
            EmoteHolder loaded = EmoteHolder.getEmoteFromUuid(this.holder.getUuid());
            return (loaded != null ? loaded : this.holder).getIconIdentifier();
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(this.changeButton, this.resetButton);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(this.changeButton, this.resetButton);
        }
    }
}
