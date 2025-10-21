package io.github.kosmx.emotes.arch.gui.widgets.search;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ISearchEngine extends Renderable {
    EditBox createEditBox(Font font, Component message, Supplier<List<EmoteListWidget.ListEntry>> entries);
    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);

    Stream<EmoteListWidget.ListEntry> filter(Stream<EmoteListWidget.ListEntry> entries, String search);

    static ISearchEngine getInstance() {
        return PlatformTools.HAS_SEARCHABLES ? new SearchablesSearch() : VanillaSearch.INSTANCE;
    }
}
