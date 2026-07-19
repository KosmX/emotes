package io.github.kosmx.emotes.arch.mixin.network;

import io.github.kosmx.emotes.arch.network.EmotesMixinNetwork;
import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public class ConnectionHandlerMixin implements EmotesMixinNetwork {
    @Unique
    private final PlayerNetworkInstance emotecraft$instance = new PlayerNetworkInstance();

    @Override
    public @NotNull PlayerNetworkInstance emotecraft$getServerNetworkInstance() {
        return this.emotecraft$instance;
    }
}
