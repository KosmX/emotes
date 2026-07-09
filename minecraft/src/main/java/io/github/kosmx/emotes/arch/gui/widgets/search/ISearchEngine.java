package io.github.kosmx.emotes.arch.gui.widgets.search;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ISearchEngine extends Renderable {
    EditBox createEditBox(Font font, Component message, Supplier<List<EmoteListWidget.ListEntry>> entries);
    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);

    void filter(EmoteListWidget.ListEntry mainFolder, String search, Consumer<EmoteListWidget.ListEntry> results);

    static ISearchEngine getInstance() {
        return PlatformTools.HAS_SEARCHABLES ? new SearchablesSearch() : VanillaSearch.INSTANCE;
    }
}
