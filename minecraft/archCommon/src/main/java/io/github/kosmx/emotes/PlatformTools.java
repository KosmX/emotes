package io.github.kosmx.emotes;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlatformTools {
    public static INetworkInstance getClientNetworkController() {
        return ClientNetwork.INSTANCE;
    }

    public static @Nullable AbstractClientPlayer getPlayerFromUUID(UUID uuid) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        return (AbstractClientPlayer) level.getPlayerByUUID(uuid);
    }

    public static void openExternalEmotesDir() {
        Util.getPlatform().openPath(InstanceService.INSTANCE.getExternalEmoteDir());
    }

    @ExpectPlatform
    public static boolean hasSearchables() {
        throw new AssertionError();
    }

    public static ClientConfig getConfig() {
        return (ClientConfig) Serializer.getConfig();
    }

    public static boolean isPlayerBlocked(UUID uuid) {
        return Minecraft.getInstance().isBlocked(uuid);
    }

    public static CameraType getPerspective() {
        return Minecraft.getInstance().options.getCameraType();
    }

    public static void setPerspective(CameraType p) {
        Minecraft.getInstance().options.setCameraType(p);
    }

    public static void addToast(Component title, Component message) {
        SystemToast.add(Minecraft.getInstance().getToastManager(), SystemToast.SystemToastId.WORLD_BACKUP, title, message);
    }

    public static void addToast(Component message) {
        PlatformTools.addToast(McUtils.MOD_NAME, message);
    }
}
