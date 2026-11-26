package io.github.kosmx.emotes;

import com.mojang.blaze3d.Blaze3D;
import com.zigythebird.playeranim.PlayerAnimLibService;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.arch.gui.toast.EmotecraftToast;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public final class PlatformTools {
    public static final boolean HAS_SEARCHABLES = PlayerAnimLibService.INSTANCE.isModLoaded("searchables");
    public static final Component REVEAL = Component.translatable(Util.getPlatform() == Util.OS.OSX ? "emotecraft.reveal.mac" : "emotecraft.reveal.other");

    public static @Nullable Avatar getAvatarFromUUID(UUID uuid) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        return (Avatar) level.getEntity(uuid);
    }

    public static void openExternalEmotesDir() {
        Blaze3D.openPath(InstanceService.INSTANCE.getExternalEmoteDir());
    }

    public static void reveal(Path path) {
        // Blaze3D.openPath would open the file itself, and java.awt.Desktop is out, as the game runs AWT headless
        Util.nonCriticalIoPool().execute(() -> {
            try {
                switch (Util.getPlatform()) {
                    case OSX -> new ProcessBuilder("open", "-R", path.toString()).start();
                    // Quoted by hand: explorer splits its switches at commas, and Java quotes only paths with spaces
                    case WINDOWS -> new ProcessBuilder("explorer.exe", "/select,", "\"" + path + "\"").start();
                    default -> {
                        if (!showItems(path)) Blaze3D.openPath(path.getParent());
                    }
                }
            } catch (IOException e) {
                CommonData.LOGGER.warn("Failed to reveal {}!", path, e);
            }
        });
    }

    private static boolean showItems(Path path) {
        String uri = path.toUri().toString().replace(",", "%2C"); // dbus-send splits arrays at commas
        try {
            // --print-reply waits for the answer, so the exit code tells whether a file manager took the call
            return new ProcessBuilder("dbus-send", "--session", "--print-reply", "--dest=org.freedesktop.FileManager1",
                    "/org/freedesktop/FileManager1", "org.freedesktop.FileManager1.ShowItems", "array:string:" + uri, "string:"
            ).start().waitFor() == 0;
        } catch (IOException e) {
            CommonData.LOGGER.debug("No dbus-send to reveal {}", path, e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
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
        EmotecraftToast.add(Minecraft.getInstance().gui.toastManager(), title, message);
    }

    public static void addToast(Component message) {
        PlatformTools.addToast(McUtils.MOD_NAME, message);
    }

    @SuppressWarnings("unused")
    public static boolean isEmoteAllowed(Animation emoteData, UUID player) {
        return !PlatformTools.getConfig().enablePlayerSafety.get() || !Minecraft.getInstance().isBlocked(player);
    }
}
