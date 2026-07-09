package io.github.kosmx.emotes.arch.gui.widgets.search;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VanillaSearch implements ISearchEngine {
    public static final VanillaSearch INSTANCE = new VanillaSearch();

    protected VanillaSearch() {
    }

    @Override
    public EditBox createEditBox(Font font, Component message, Supplier<List<EmoteListWidget.ListEntry>> entries) {
        return new EditBox(font, 0, 0, Button.BIG_WIDTH, Button.DEFAULT_HEIGHT, message);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // no-op
    }

    @Override
    public void filter(EmoteListWidget.ListEntry mainFolder, String search, Consumer<EmoteListWidget.ListEntry> results) {
        mainFolder.searchFor(search, entry -> entry.matches(search.toLowerCase()), results);
    }
}
