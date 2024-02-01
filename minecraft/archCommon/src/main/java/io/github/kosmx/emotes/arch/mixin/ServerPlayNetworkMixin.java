package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinNetwork;
import io.github.kosmx.emotes.arch.network.ModdedServerPlayNetwork;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkMixin extends ServerCommonPacketListenerImpl implements EmotesMixinNetwork {

    @Unique
    @NotNull
    private final IServerNetworkInstance emotesNetworkInstance = new ModdedServerPlayNetwork((ServerGamePacketListenerImpl)(Object) this);

    @Override
    public @NotNull IServerNetworkInstance emotecraft$getServerNetworkInstance() {
        return emotesNetworkInstance;
    }

    public ServerPlayNetworkMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }
}
