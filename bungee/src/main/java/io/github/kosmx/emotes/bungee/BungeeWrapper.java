package io.github.kosmx.emotes.bungee;

import io.github.kosmx.emotes.bungee.executor.BungeeInstance;
import io.github.kosmx.emotes.bungee.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeWrapper extends Plugin {
    public final static String EmotePacket = CommonData.getIDAsString(CommonData.playEmoteID);
    public final static String GeyserPacket = "geyser:emote";
    ServerSideEmotePlay networkPlay = null;

    @Override
    public void onLoad() {
        if (CommonData.isLoaded) {
            getLogger().warning("Emotecraft is loaded multiple times, please load it only once!");
            this.onDisable(); // disable itself.
        } else {
            CommonData.isLoaded = true;
        }
        EmoteInstance.instance = new BungeeInstance(this);
        Serializer.INSTANCE = new Serializer(); // it does register itself
        EmoteInstance.config = Serializer.getConfig();
        UniversalEmoteSerializer.loadEmotes();
    }

    @Override
    public void onEnable() {
        this.networkPlay = new ServerSideEmotePlay(this);
        getProxy().getPluginManager().registerListener(this, networkPlay);
        super.onEnable();
        getLogger().info("Loading Emotecraft as a bukkit plugin...");
        getLogger().warning("Emotecraft is meant to be used on a server, not on a proxy.");
        getLogger().warning("Certain features will be unavailable, like server-side API.");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getProxy().unregisterChannel(EmotePacket);
    }
}