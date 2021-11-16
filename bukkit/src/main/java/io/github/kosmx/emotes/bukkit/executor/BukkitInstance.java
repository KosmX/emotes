package io.github.kosmx.emotes.bukkit.executor;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.Logger;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;

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
    public IDefaultTypes getDefaults() {
        return null;
    }

    @Override
    public IGetters getGetters() {
        return null;
    }

    @Override
    public IClientMethods getClientMethods() {
        return null;
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
