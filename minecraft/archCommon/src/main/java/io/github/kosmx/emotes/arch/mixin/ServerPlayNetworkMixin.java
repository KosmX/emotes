package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinConnection;
import io.github.kosmx.emotes.arch.network.EmotesMixinNetwork;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkMixin extends ServerCommonPacketListenerImpl implements EmotesMixinNetwork {

    @Unique
    @NotNull
    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();

    @Override
    public @NotNull EmotePlayTracker emotecraft$getEmoteTracker() {
        return emoteTracker;
    }

    @Override
    public @NotNull EmotesMixinConnection emotecraft$getConnection() {
        return this.connection;
    }

    public ServerPlayNetworkMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }
}
