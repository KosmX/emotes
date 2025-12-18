package io.github.kosmx.emotes;

import com.zigythebird.playeranim.PlayerAnimLibPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlatformTools {
    public static final boolean HAS_SEARCHABLES = PlayerAnimLibPlatform.isModLoaded("searchables");

    public static INetworkInstance getClientNetworkController() {
        return ClientNetwork.INSTANCE;
    }

    public static @Nullable Avatar getAvatarFromUUID(UUID uuid) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        return (Avatar) level.getEntity(uuid);
    }

    public static void openExternalEmotesDir() {
        Util.getPlatform().openPath(InstanceService.INSTANCE.getExternalEmoteDir());
    }

    public static ClientConfig getConfig() {
        return (ClientConfig) Serializer.getConfig();
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
