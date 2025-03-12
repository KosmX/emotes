package io.github.kosmx.emotes.arch.gui.widgets.search;

import com.blamejared.searchables.api.SearchableComponent;
import com.blamejared.searchables.api.SearchableType;
import com.blamejared.searchables.api.autcomplete.AutoCompletingEditBox;
import com.blamejared.searchables.api.context.ContextVisitor;
import com.blamejared.searchables.api.context.SearchContext;
import com.blamejared.searchables.lang.StringSearcher;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SearchablesSearch implements ISearchEngine {
    public static final SearchableType<EmoteListWidget.EmoteEntry> SEARCHABLE = new SearchableType.Builder<EmoteListWidget.EmoteEntry>()
            .defaultComponent(SearchableComponent.create("default",
                    holder -> Optional.ofNullable(holder.emote.name)
                            .map(Component::getString)
                            .filter(str -> !str.isEmpty()),
                    ISearchEngine::matches
            ))
            .component(SearchableComponent.create("name",
                    holder -> Optional.ofNullable(holder.emote.name)
                            .map(Component::getString)
                            .filter(str -> !str.isEmpty())
            ))
            .component(SearchableComponent.create("description",
                    holder -> Optional.ofNullable(holder.emote.description)
                            .map(Component::getString)
                            .filter(str -> !str.isEmpty())
            ))
            .component(SearchableComponent.create("author",
                    holder -> Optional.ofNullable(holder.emote.author)
                            .map(Component::getString)
                            .filter(str -> !str.isEmpty())
            ))
            .build();

    protected AutoCompletingEditBox<EmoteListWidget.EmoteEntry> search;

    @Override
    public EditBox createEditBox(Font font, Component message, Supplier<List<EmoteListWidget.EmoteEntry>> entries) {
        return this.search = new FixedAutoCompletingEditBox<>(font, 0, 0, Button.BIG_WIDTH, Button.DEFAULT_HEIGHT, message,
                SEARCHABLE, entries
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.search == null) {
            return false;
        }
        return this.search.autoComplete().mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.search != null) {
            this.search.autoComplete().render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public Stream<EmoteListWidget.EmoteEntry> filter(Stream<EmoteListWidget.EmoteEntry> entries, String search) {
        Optional<SearchContext<EmoteListWidget.EmoteEntry>> context = StringSearcher.search(search, new ContextVisitor<>());
        return entries.filter(
                context.map(tSearchContext ->
                        tSearchContext.createPredicate(SEARCHABLE)
                ).orElse(t -> true)
        );
    }

    public static class FixedAutoCompletingEditBox<T> extends AutoCompletingEditBox<T> {
        public FixedAutoCompletingEditBox(Font font, int x, int y, int width, int height, Component message, SearchableType<T> type, Supplier<List<T>> entries) {
            super(font, x, y, width, height, message, type, entries);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isHoveredOrFocused()) return false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void setX(int x) {
            autoComplete().setX(x);
            super.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            autoComplete().setY(getY() + 2 + getHeight());
        }

        @Override
        public void setWidth(int width) {
            autoComplete().setWidth(width);
            super.setWidth(width);
        }

        @Override
        public void setHeight(int height) {
            autoComplete().setHeight(height);
            super.setHeight(height);
            autoComplete().setY(getY() + 2 + getHeight());
        }

        @Override
        public void setSize(int width, int height) {
            autoComplete().setSize(width, height);
            super.setSize(width, height);
            autoComplete().setY(getY() + 2 + getHeight());
        }
    }
}
