package com.kosmx.emotes.fabric;

import com.kosmx.emotes.fabric.gui.EmoteMenuImpl;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenu implements ModMenuApi {
    /*
    ConfigScreenFactory was used in the old config //TODO remove me
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (ConfigScreenFactory<Screen>) EmoteMenuImpl::new; //TODO
    }
}
