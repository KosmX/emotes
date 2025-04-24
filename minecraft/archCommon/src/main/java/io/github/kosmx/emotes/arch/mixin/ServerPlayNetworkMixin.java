package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinNetwork;
import io.github.kosmx.emotes.arch.network.ModdedServerPlayNetwork;
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
    private final ModdedServerPlayNetwork emotecraft$instance = new ModdedServerPlayNetwork((ServerGamePacketListenerImpl)(Object) this);

    public ServerPlayNetworkMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Override
    public @NotNull ModdedServerPlayNetwork emotecraft$getServerNetworkInstance() {
        return this.emotecraft$instance;
    }
}
