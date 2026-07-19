package io.github.kosmx.emotes.arch.mixin.network;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.arch.network.ducks.ConfigEmotesNetworkMixin;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.util.function.Consumer;

@Mixin(Connection.class)
public class ConnectionHandlerMixin implements ConfigEmotesNetworkMixin {
    @Unique
    private final ConfigNetworkInstance emotecraft$instance = new ConfigNetworkInstance();

    @Override
    public void emotecraft$receiveConfigMessage(EmotePacket packet, Consumer<Packet<?>> consumer) throws IOException {
        this.emotecraft$instance.receiveConfigMessage(packet, emotePacket ->
                consumer.accept(NetworkPlatformTools.playPacket(emotePacket))
        );
    }

    @Override
    public @NotNull ConfigNetworkInstance emotecraft$getConfigNetworkInstance() {
        return this.emotecraft$instance;
    }
}
