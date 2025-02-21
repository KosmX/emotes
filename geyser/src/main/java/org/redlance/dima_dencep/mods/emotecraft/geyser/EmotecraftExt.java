package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.api.services.LoggerService;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;

import java.util.logging.Level;

public class EmotecraftExt implements Extension {
    private static EmotecraftExt instance;

    public EmotecraftExt() {
        EmotecraftExt.instance = this;
    }

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        LoggerService.INSTANCE.log(Level.INFO, "Loading emotecraft on geyser...");
    }

    public static EmotecraftExt getInstance() {
        return EmotecraftExt.instance;
    }
}
