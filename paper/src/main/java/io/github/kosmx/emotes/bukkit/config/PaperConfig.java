package io.github.kosmx.emotes.bukkit.config;

import io.github.kosmx.emotes.server.config.CommonConfig;

public class PaperConfig extends CommonConfig {
    public final ConfigEntry<Boolean> suppressProxyWarning = new ConfigEntry<>("suppressProxyWarning", false, false, category("paper"), true);
}
