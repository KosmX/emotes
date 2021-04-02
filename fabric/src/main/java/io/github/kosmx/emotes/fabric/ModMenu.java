package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.fabric.gui.EmoteMenuImpl;
import net.minecraft.client.gui.screens.Screen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {
    /*
    ConfigScreenFactory was used in the old config //TODO remove me
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (ConfigScreenFactory<Screen>) EmoteMenuImpl::new; //TODO
    }
}
