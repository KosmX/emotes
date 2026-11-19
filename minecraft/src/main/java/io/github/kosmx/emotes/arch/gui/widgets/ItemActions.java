package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.Blaze3D;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.dropdown.AppDropdownMenu;
import io.github.kosmx.emotes.arch.library.modals.ShareScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class ItemActions {
    private static final Component PLAY_WITHOUT_SOUND = Component.translatable("emotecraft.playWithoutSound");

    private final AppDropdownMenu.MenuScope menu;
    private final Component name;
    private final Component author;
    private final Runnable closeMenu;

    public ItemActions(AppDropdownMenu.MenuScope menu, Component name, Component author, Runnable closeMenu) {
        this.menu = menu;
        this.name = name;
        this.author = author;
        this.closeMenu = closeMenu;
    }

    public void addEmote(@Nullable Runnable playWithoutSound, @Nullable URI link, Supplier<ShareScreen.Icon> icon, @Nullable Path path) {
        if (playWithoutSound != null) this.menu.group(group -> add(group, PLAY_WITHOUT_SOUND, playWithoutSound));
        if (link != null) {
            addLinks(link, icon, path);
        } else {
            addReveal(path);
        }
    }

    public void addLinks(URI link, Supplier<ShareScreen.Icon> icon, @Nullable Path path) {
        Minecraft minecraft = Minecraft.getInstance();
        this.menu.group(group -> add(group, ShareScreen.SHARE, () -> minecraft.gui.setScreen(new ShareScreen(minecraft.gui.screen(), this.name, this.author, link, icon.get()))));
        this.menu.group(group -> {
            add(group, CommonComponents.GUI_OPEN_IN_BROWSER, () -> Blaze3D.openUri(link));
            addReveal(group, path);
        });
    }

    public void addReveal(@Nullable Path path) {
        this.menu.group(group -> addReveal(group, path));
    }

    private void addReveal(AppDropdownMenu.GroupScope group, @Nullable Path path) {
        if (path != null) add(group, PlatformTools.REVEAL, () -> PlatformTools.reveal(path));
    }

    private void add(AppDropdownMenu.GroupScope group, Component text, Runnable action) {
        group.item(text, () -> {
            this.closeMenu.run();
            action.run();
        });
    }
}
