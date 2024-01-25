package io.github.kosmx.emotes.arch.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class EmoteConfigScreen extends Screen {
    private final @Nullable Screen parent;


    protected EmoteConfigScreen(@NotNull Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected int getWidth() {
        return this.width;
    }

    protected int getHeight() {
        return this.height;
    }

    @Override
    public void onClose() {
        getMinecraft().setScreen(parent);
    }

    protected @Nullable Screen getParent() {
        return parent;
    }

    protected <T extends GuiEventListener & NarratableEntry> void addToChildren(T widget) {
        addWidget(widget);
    }

    protected @NotNull Minecraft getMinecraft() {
        return Objects.requireNonNullElseGet(minecraft, Minecraft::getInstance);
    }

    protected void openParent() {
        getMinecraft().setScreen(parent);
    }
}
