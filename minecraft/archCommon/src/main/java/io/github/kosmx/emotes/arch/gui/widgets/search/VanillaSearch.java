package io.github.kosmx.emotes.arch.gui.widgets.search;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class VanillaSearch implements ISearchEngine {
    public static final VanillaSearch INSTANCE = new VanillaSearch();

    protected VanillaSearch() {
    }

    @Override
    public EditBox createEditBox(Font font, Component message, Supplier<List<EmoteListWidget.EmoteEntry>> entries) {
        return new EditBox(font, 0, 0, Button.BIG_WIDTH, Button.DEFAULT_HEIGHT, message);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // no-op
    }

    @Override
    public Stream<EmoteListWidget.EmoteEntry> filter(Stream<EmoteListWidget.EmoteEntry> entries, String search) {
        return entries.filter(entry -> ISearchEngine.matches(entry, search));
    }
}
