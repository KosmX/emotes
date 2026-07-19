package io.github.kosmx.emotes.arch.mixin.network;

import io.github.kosmx.emotes.arch.network.EmotesMixinNetwork;
import io.github.kosmx.emotes.arch.network.server.instance.PlayerNetworkInstance;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements EmotesMixinNetwork {
    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @Inject(
            method = "<init>",
            at = @At("CTOR_HEAD")
    )
    public void emotecraft$setPlayer(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        emotecraft$getServerNetworkInstance().setServerGamePacketListener((ServerGamePacketListenerImpl) (Object) this);
    }

    @Override
    public @NotNull PlayerNetworkInstance emotecraft$getServerNetworkInstance() {
        return this.connection.emotecraft$getServerNetworkInstance();
    }
}
