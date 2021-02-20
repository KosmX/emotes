package com.kosmx.emotes.main.config;

import com.kosmx.emotes.gui.EmoteMenu;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class Menu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory(){
        return EmoteMenu::new;
    }
}
