package io.github.kosmx.emotes.arch.mixin.network;

import io.github.kosmx.emotes.arch.network.ducks.GameEmotesNetworkMixin;
import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements GameEmotesNetworkMixin {
    @Unique
    private final PlayerNetworkInstance emotecraft$instance = new PlayerNetworkInstance(
            (ServerGamePacketListenerImpl) (Object) this, this.connection.emotecraft$getConfigNetworkInstance()
    );

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @Override
    public @NotNull PlayerNetworkInstance emotecraft$getGameNetworkInstance() {
        return this.emotecraft$instance;
    }
}
