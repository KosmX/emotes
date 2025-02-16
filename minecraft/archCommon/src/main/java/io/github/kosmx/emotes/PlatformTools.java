package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
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
        Util.getPlatform().openFile(EmoteInstance.instance.getExternalEmoteDir());
    }

    @ExpectPlatform
    public static boolean hasSearchables() {
        throw new AssertionError();
    }
}
