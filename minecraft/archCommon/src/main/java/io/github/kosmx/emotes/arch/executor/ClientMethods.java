package io.github.kosmx.emotes.arch.executor;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.executor.types.ImplNativeImageBackedTexture;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public final class ClientMethods {
    public static int tick = 0;

    public void destroyTexture(ResourceLocation identifier) {
        Minecraft.getInstance().getTextureManager().release(identifier);
    }

    public void registerTexture(ResourceLocation identifier, ImplNativeImageBackedTexture nativeImageBacketTexture) {
        Minecraft.getInstance().getTextureManager().register(identifier, nativeImageBacketTexture.get());
    }

    public ImplNativeImageBackedTexture readNativeImage(InputStream inputStream) throws IOException {
        return new ImplNativeImageBackedTexture(new DynamicTexture(NativeImage.read(inputStream)));
    }

    public boolean isAbstractClientEntity(Object entity) {
        return entity instanceof AbstractClientPlayer && Minecraft.getInstance().player == entity; //make sure it'll work
    }

    public IPlayerEntity getMainPlayer() {
        return (IPlayerEntity) Minecraft.getInstance().player;
    }

    public INetworkInstance getServerNetworkController() {
        return PlatformTools.getClientNetworkController();
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
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.WORLD_BACKUP, text, Component.literal(msg));
    }
}
