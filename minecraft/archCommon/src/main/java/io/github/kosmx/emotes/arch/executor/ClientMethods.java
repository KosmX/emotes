package io.github.kosmx.emotes.arch.executor;

import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class ClientMethods {
    public static int tick = 0;

    public boolean isAbstractClientEntity(Object entity) {
        return entity instanceof AbstractClientPlayer && Minecraft.getInstance().player == entity; //make sure it'll work
    }

    public IPlayerEntity getMainPlayer() {
        return (IPlayerEntity) Minecraft.getInstance().player;
    }

    public int getCurrentTick() {
        return tick;
    }

    public boolean isPlayerBlocked(UUID uuid) {
        return Minecraft.getInstance().isBlocked(uuid);
    }

    public int getPerspective() {
        return Minecraft.getInstance().options.getCameraType().ordinal();
    }

    public void setPerspective(int p) {
        Minecraft.getInstance().options.setCameraType(CameraType.values()[p]);
    }

    public void sendChatMessage(Component msg) {
        Minecraft.getInstance().gui.getChat().addMessage(msg);
    }

    public void toastExportMessage(int level, Component text, String msg) {
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastId.WORLD_BACKUP, text, Component.literal(msg));
    }
}
