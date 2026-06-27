package io.github.kosmx.emotes;

import com.zigythebird.playeranim.PlayerAnimLibService;
import com.zigythebird.playeranimcore.animation.Animation;
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
    public static final boolean HAS_SEARCHABLES = PlayerAnimLibService.INSTANCE.isModLoaded("searchables");

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

    public static CameraType getCameraType() {
        return Minecraft.getInstance().options.getCameraType();
    }

    public static void setCameraType(CameraType p) {
        Minecraft.getInstance().options.setCameraType(p);
    }

    public static void addToast(Component title, Component message) {
        SystemToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(), SystemToast.SystemToastId.WORLD_BACKUP, title, message);
    }

    public static void addToast(Component message) {
        PlatformTools.addToast(McUtils.MOD_NAME, message);
    }

    @SuppressWarnings("unused")
    public static boolean isEmoteAllowed(Animation emoteData, UUID player) {
        return !PlatformTools.getConfig().enablePlayerSafety.get() || !Minecraft.getInstance().isBlocked(player);
    }
}
