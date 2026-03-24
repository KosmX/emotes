package io.github.kosmx.emotes.arch.screen.utils;

import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Optional;

public enum EmotecraftTexture {
    HEADER_SEPARATOR(Screen.HEADER_SEPARATOR, Screen.INWORLD_HEADER_SEPARATOR),
    FOOTER_SEPARATOR(Screen.FOOTER_SEPARATOR, Screen.INWORLD_FOOTER_SEPARATOR),
    MENU_LIST_BACKGROUND(AbstractSelectionList.MENU_LIST_BACKGROUND, AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND);

    private final Identifier minecraft;
    private final Identifier minecraftInWorld;

    private final Identifier emotecraft;
    private final Identifier emotecraftInWorld;

    EmotecraftTexture(Identifier minecraft, Identifier minecraftInWorld) {
        this(minecraft, minecraftInWorld, McUtils.newIdentifier(minecraft.getPath()), McUtils.newIdentifier(minecraftInWorld.getPath()));
    }

    EmotecraftTexture(Identifier minecraft, Identifier minecraftInWorld, Identifier emotecraft, Identifier emotecraftInWorld) {
        this.minecraft = minecraft;
        this.minecraftInWorld = minecraftInWorld;
        this.emotecraft = emotecraft;
        this.emotecraftInWorld = emotecraftInWorld;
    }

    public Identifier identifier(boolean inWorld) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (inWorld) {
            Optional<Resource> resourceInWorld = manager.getResource(this.emotecraftInWorld);
            if (resourceInWorld.isPresent()) return this.emotecraftInWorld;

            Optional<Resource> resource = manager.getResource(this.emotecraft);
            if (resource.isPresent()) return this.emotecraft;

            return this.minecraftInWorld;
        } else {
            Optional<Resource> resource = manager.getResource(this.emotecraft);
            return resource.isEmpty() ? this.minecraft : this.emotecraft;
        }
    }
}
