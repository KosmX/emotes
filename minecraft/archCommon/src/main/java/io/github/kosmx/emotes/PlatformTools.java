package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlatformTools {
    public static INetworkInstance getClientNetworkController() {
        return ClientNetwork.INSTANCE;
    }

    public static @Nullable IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null) return null;
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }

    public static void openExternalEmotesDir() {
        Util.getPlatform().openPath(InstanceService.LOADED_SERVICE.getExternalEmoteDir());
    }

    @ExpectPlatform
    public static boolean hasSearchables() {
        throw new AssertionError();
    }

    public static ClientConfig getConfig() {
        return (ClientConfig) Serializer.getConfig();
    }
}
