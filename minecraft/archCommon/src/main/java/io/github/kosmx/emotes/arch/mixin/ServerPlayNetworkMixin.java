package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinNetworkAccessor;
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
public abstract class ServerPlayNetworkMixin extends ServerCommonPacketListenerImpl implements EmotesMixinNetworkAccessor {

    @Unique
    @NotNull
    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();

    @Override
    public @NotNull EmotePlayTracker emotecraft$getEmoteTracker() {
        return emoteTracker;
    }

    @Unique
    @NotNull
    private final HashMap<Byte, Byte> versions = new HashMap<>();

    @Override
    public @NotNull HashMap<Byte, Byte> emotecraft$getRemoteVersions() {
        return versions;
    }

    @Override
    public void emotecraft$setVersions(@Nullable HashMap<Byte, Byte> map) {
        versions.clear();
        if (map != null) {
            versions.putAll(map);
        } 
    }

    public ServerPlayNetworkMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }
}
