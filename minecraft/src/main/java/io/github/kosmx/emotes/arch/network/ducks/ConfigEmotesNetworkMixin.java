package io.github.kosmx.emotes.arch.network.ducks;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public interface ConfigEmotesNetworkMixin {
    default void emotecraft$receiveConfigMessage(EmotePacket packet, Consumer<Packet<?>> consumer) throws IOException {
        throw new AssertionError();
    }

    @NotNull
    default ConfigNetworkInstance emotecraft$getConfigNetworkInstance() {
        throw new AssertionError();
    }
}
