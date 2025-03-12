package io.github.kosmx.emotes.arch.screen.components;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Like {@link OptionsSubScreen} but with emotes.
 * Use to create your list of emotes. (dima_dencep uses it)
 */
public abstract class EmoteSubScreen extends Screen {
    private static final Component SEARCH = Component.translatable("gui.recipebook.search_hint");

    protected final ISearchEngine searchEngine;
    protected Screen lastScreen;

    @Nullable
    protected PlayerPreview preview;
    @Nullable
    protected EmoteListWidget list;
    protected HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    protected EditBox searchBox;

    protected EmoteSubScreen(Component title, Screen lastScreen) {
        this(title, ISearchEngine.getInstance(), lastScreen);
    }

    protected EmoteSubScreen(Component title, ISearchEngine searchEngine, Screen lastScreen) {
        super(title);
        this.searchEngine = searchEngine;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addPlayerPreview();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        this.searchBox = this.layout.addToHeader(this.searchEngine.createEditBox(this.font, SEARCH,
                () -> Objects.requireNonNull(this.list).getEmotes(this.searchBox != null && !StringUtils.isBlank(this.searchBox.getValue()))
        ));
        this.searchBox.setResponder((string) -> Objects.requireNonNull(this.list).filter(this.searchEngine, this.searchBox != null && !StringUtils.isBlank(this.searchBox.getValue()), string));
    }

    protected void addPlayerPreview() {
        this.preview = this.layout.addToContents(new PlayerPreview(
                this.minecraft.getGameProfile(), 0, 0, 0, 0, true
        ), layoutSettings -> layoutSettings.alignHorizontallyLeft().paddingLeft(Button.DEFAULT_SPACING));
    }

    protected EmoteListWidget newEmoteListWidget() {
        return new EmoteListWidget(
                this.minecraft, width, this.layout.getContentHeight(), this.layout.getHeaderHeight(), 36
        ) {
            @Override
            public void setSelected(@Nullable EmoteListWidget.ListEntry entry) {
                super.setSelected(entry);
                onPressed(entry);
            }

            @Override
            public boolean setLastFolder(FolderEntry folder) {
                if (super.setLastFolder(folder)) {
                    if (searchBox != null) searchBox.setValue("");
                    if (preview != null) preview.getPlayer().stopEmote();
                    return true;
                }
                return false;
            }
        };
    }

    protected void addContents() {
        this.list = this.layout.addToContents(newEmoteListWidget());
        addOptions();
    }

    protected abstract void addOptions();

    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        if (this.list != null) linearLayout.addChild(this.list.createBackButton());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE,
                button -> onClose()
        ).width(200).build());
    }

    protected abstract void onPressed(@Nullable EmoteListWidget.ListEntry selected);

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.preview != null) {
            this.preview.setSize(width / 6, height / 2);
        }
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);

            if (this.preview != null) { // For small screens
                this.preview.visible = this.preview.getRight() <= this.list.getRowLeft();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.preview != null) {
            EmoteListWidget.ListEntry hovered = this.list.getHovered();
            if (this.list.getSelected() == hovered) {
                hovered = null;
            }
            if (hovered instanceof EmoteListWidget.EmoteEntry emote) {
                this.preview.playAnimation(emote.emote.emote, true);
            } else if (hovered instanceof EmoteListWidget.FolderEntry) {
                this.preview.getPlayer().stopEmote();
            }
            this.preview.tick();
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.searchEngine.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.searchEngine.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
