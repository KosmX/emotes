package io.github.kosmx.emotes.bukkit.executor;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BukkitInstance extends EmoteInstance {
    final java.util.logging.Logger logger;
    final BukkitWrapper plugin;

    public BukkitInstance(BukkitWrapper plugin){
        this.logger = plugin.getLogger();
        this.plugin = plugin;
    }

    @Override
    public Logger getLogger() {
        return this.logger::log;
    }


    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public Path getGameDirectory() {
        return Paths.get("");
    }
}
