package com.kosmx.emotecraft.config;

import com.kosmx.emotecraft.gui.EmoteMenu;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class Menu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EmoteMenu::new;
    }
}
