package io.github.kosmx.emotes.arch.executor;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.arch.executor.types.ImplNativeImageBackedTexture;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.inline.dataTypes.IIdentifier;
import io.github.kosmx.emotes.inline.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.inline.dataTypes.Text;
import io.github.kosmx.emotes.inline.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@SuppressWarnings("unchecked")
public final class ClientMethods {
    public static int tick = 0;

    public void destroyTexture(IIdentifier identifier) {
        Minecraft.getInstance().getTextureManager().release(((IdentifierImpl)identifier).get());
    }

    public void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture) {
        Minecraft.getInstance().getTextureManager().register(((IdentifierImpl)identifier).get(), ((ImplNativeImageBackedTexture)nativeImageBacketTexture).get());
    }

    public INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException {
        return new ImplNativeImageBackedTexture(new DynamicTexture(NativeImage.read(inputStream)));
    }

    public boolean isAbstractClientEntity(Object entity) {
        return entity instanceof AbstractClientPlayer && Minecraft.getInstance().player == entity; //make sure it'll work
    }

    public void openScreen(IScreen screen) {
        Minecraft.getInstance().setScreen(((IScreen<Screen>)screen).getScreen());
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

    public void sendChatMessage(Text msg) {
        Minecraft.getInstance().gui.getChat().addMessage(((TextImpl)msg).get());
    }

    public void toastExportMessage(int level, Text text, String msg) {
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.WORLD_BACKUP, ((TextImpl)text).get(), Component.literal(msg));
    }
}
