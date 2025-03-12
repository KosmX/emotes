package io.github.kosmx.emotes.arch.screen.components;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(Button.DEFAULT_SPACING));

        this.searchBox = linearLayout.addChild(this.searchEngine.createEditBox(this.font, SEARCH,
                () -> Objects.requireNonNull(this.list).getEmotes()
        ));
        this.searchBox.setResponder((string) -> Objects.requireNonNull(this.list).filter(this.searchEngine, string));

        linearLayout.addChild(new StringWidget(Component.literal("/ 2342342 / 2342342 / 234234"), font),
                LayoutSettings::alignHorizontallyLeft
        );
    }

    /*private static MutableComponent appendScreenPath(FullMenuScreen screen, MutableComponent component) {
        component = component.append(SLASH).append(CommonComponents.SPACE).append(screen.path);

        if (screen.lastScreen instanceof FullMenuScreen parent) {
            return appendScreenPath(parent, component.append(CommonComponents.SPACE));
        } else {
            return component;
        }
    }*/

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
                if (entry instanceof FolderEntry && searchBox != null) {
                    searchBox.setValue("");
                }
            }
        };
    }

    protected void addContents() {
        this.list = this.layout.addToContents(newEmoteListWidget());
        addOptions();
    }

    protected abstract void addOptions();

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
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
