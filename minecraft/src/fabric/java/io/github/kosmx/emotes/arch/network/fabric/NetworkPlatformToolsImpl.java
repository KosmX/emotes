package io.github.kosmx.emotes.arch.network.fabric;

import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import io.github.kosmx.emotes.fabric.EmotecraftFabricMod;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public final class NetworkPlatformToolsImpl implements NetworkPlatformTools {
    @Override
    public boolean canSendPlay(ServerPlayer player, Identifier channel) {
        return ServerPlayNetworking.canSend(player, channel);
    }

    @Override
    public Collection<ServerPlayer> getTrackedBy(Entity entity) {
        return PlayerLookup.tracking(entity);
    }

    @Override
    public MinecraftServer getServer() {
        return EmotecraftFabricMod.SERVER_INSTANCE;
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
