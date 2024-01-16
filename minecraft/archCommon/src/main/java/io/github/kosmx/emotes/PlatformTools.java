package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlatformTools {

    @ExpectPlatform
    public static boolean isPlayerAnimLoaded() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static INetworkInstance getClientNetworkController() {
        throw new AssertionError();
    }


    public static @Nullable IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null) return null;
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }
}
