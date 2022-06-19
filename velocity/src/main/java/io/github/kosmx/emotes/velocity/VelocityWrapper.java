package io.github.kosmx.emotes.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.velocity.executor.VelocityInstance;
import io.github.kosmx.emotes.velocity.network.ServerSideEmotePlay;

import java.util.logging.Logger;

@Plugin(
        id = "emotecraft",
        version = "unspecified",
        name = "emotecraft"
)
public class VelocityWrapper {

    public final static ChannelIdentifier EmotePacket =
            MinecraftChannelIdentifier.create(CommonData.MOD_ID, CommonData.playEmoteID);

    public final static ChannelIdentifier GeyserPacket =
            MinecraftChannelIdentifier.create("geyser", "emote");

    private final ProxyServer server;
    private final Logger logger;

    private ServerSideEmotePlay networkPlay;

    @Inject
    public VelocityWrapper(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void init(ProxyInitializeEvent event) {
        if (CommonData.isLoaded) {
            logger.warning("Emotecraft is loaded multiple times, please load it only once!");
            this.shutdown(); // disable itself.
        } else {
            CommonData.isLoaded = true;
        }
        EmoteInstance.instance = new VelocityInstance(logger::log);
        Serializer.INSTANCE = new Serializer(); // it does register itself
        EmoteInstance.config = Serializer.getConfig();
        UniversalEmoteSerializer.loadEmotes();
        this.networkPlay = new ServerSideEmotePlay(server);
        server.getEventManager().register(this, networkPlay);
        logger.info("Loading Emotecraft as a velocity plugin...");

        logger.warning("Emotecraft is meant to be used on a server, not on a proxy.");
        logger.warning("Certain features will be unavailable, like server-side API.");
    }

    public void shutdown() {
        server.getChannelRegistrar().unregister(EmotePacket);
    }

    @Subscribe
    public void shutdown(ProxyShutdownEvent event) {
        shutdown();
    }

}
