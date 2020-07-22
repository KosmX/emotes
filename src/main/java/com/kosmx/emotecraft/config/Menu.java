package com.kosmx.emotecraft.config;

import com.kosmx.emotecraft.screen.EmoteMenu;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;

public class Menu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EmoteMenu::new;
    }
}
