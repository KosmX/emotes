package io.github.kosmx.emotes.arch.screen.components;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import io.github.kosmx.emotes.arch.screen.utils.EmoteListener;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import io.github.kosmx.emotes.server.serializer.EmoteWriter;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Like {@link OptionsSubScreen} but with emotes.
 * Use to create your list of emotes. (dima_dencep uses it)
 */
public abstract class EmoteSubScreen extends Screen {
    protected final boolean reloadOnOpen;
    protected final ISearchEngine searchEngine;
    protected Screen lastScreen;

    @Nullable
    public EmoteListener watcher;
    @Nullable
    protected PlayerPreview preview;
    @Nullable
    protected EmoteListWidget list;
    protected HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    protected EditBox searchBox;

    protected EmoteSubScreen(Component title, boolean reloadOnOpen, Screen lastScreen) {
        this(title, reloadOnOpen, ISearchEngine.getInstance(), lastScreen);
    }

    protected EmoteSubScreen(Component title, boolean reloadOnOpen, ISearchEngine searchEngine, Screen lastScreen) {
        super(title);
        this.reloadOnOpen = reloadOnOpen;
        this.searchEngine = searchEngine;
        this.lastScreen = lastScreen;
    }

    @Override
    public void added() {
        if (this.watcher == null) {
            this.watcher = EmoteListener.create(InstanceService.INSTANCE.getExternalEmoteDir());
            if (this.reloadOnOpen && this.watcher != null) this.watcher.load(this::addOptions, this.minecraft);
        }
        super.added();
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
        this.searchBox = this.layout.addToHeader(this.searchEngine.createEditBox(this.font, RecipeBookComponent.SEARCH_HINT,
                () -> Objects.requireNonNull(this.list).getEmotes(isSearchActive())
        ));
        this.searchBox.setHint(RecipeBookComponent.SEARCH_HINT);
        this.searchBox.setResponder((string) -> Objects.requireNonNull(this.list).filter(this.searchEngine, isSearchActive(), string));
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
                    if (preview != null) preview.getMannequin().stopEmote();
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
        if (this.watcher != null) {
            try {
                if (this.watcher.pollForChanges()) {
                    this.watcher.load(this::addOptions, this.minecraft);
                }
            } catch (IOException ex) {
                CommonData.LOGGER.warn("Failed to poll for directory changes, stopping", ex);
                this.closeWatcher();
            }
        }
        super.tick();
        if (this.preview != null) {
            EmoteListWidget.ListEntry hovered = this.list.getHovered();
            if (this.list.getSelected() == hovered) {
                hovered = null;
            }
            if (hovered instanceof EmoteListWidget.EmoteEntry emote) {
                this.preview.playAnimation(emote.emote.emote, Animation.LoopType.DEFAULT, true);
            } else if (hovered instanceof EmoteListWidget.FolderEntry) {
                this.preview.getMannequin().stopEmote();
            }
            this.preview.tick();
        }
    }

    @Override
    public void removed() {
        if (this.watcher != null) this.watcher.blockWhileLoading();
        super.removed();
        if (this.preview != null) this.preview.getMannequin().stopEmote();
    }

    private void closeWatcher() {
        if (this.watcher != null) {
            try {
                this.watcher.close();
                this.watcher = null;
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to close watcher!", th);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.watcher != null && this.watcher.isLoading()) {
            PlatformTools.addToast(EmoteListener.RELOADING_WAIT);
            return;
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.watcher == null || !this.watcher.isLoading();
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

    @Override
    public void onFilesDrop(List<Path> paths) {
        for (Path path : paths) {
            try (Stream<Path> stream = Files.walk(path)) {
                stream.forEach(emote -> {
                    Map<String, Animation> animations = EmoteSerializer.serializeExternalEmote(emote);
                    if (animations.isEmpty()) return;

                    for (Animation animation : animations.values()) {
                        try {
                            EmoteWriter.writeAnimationInBestFormat(animation, InstanceService.INSTANCE.getExternalEmoteDir());
                        } catch (Throwable th) {
                            CommonData.LOGGER.warn("Failed to move animation!", th);
                        }
                    }
                });
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to walk!", th);
            }
        }
    }

    public boolean isSearchActive() {
        return this.searchBox != null && !StringUtils.isBlank(this.searchBox.getValue());
    }
}
