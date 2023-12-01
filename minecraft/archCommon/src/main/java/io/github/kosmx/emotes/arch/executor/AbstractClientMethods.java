package io.github.kosmx.emotes.arch.executor;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import io.github.kosmx.emotes.arch.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.arch.executor.types.ImplNativeImageBackedTexture;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
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
public abstract class AbstractClientMethods implements IClientMethods {
    public static int tick = 0;

    @Override
    public void destroyTexture(IIdentifier identifier) {
        Minecraft.getInstance().getTextureManager().release(((IdentifierImpl)identifier).get());
    }

    @Override
    public void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture) {
        Minecraft.getInstance().getTextureManager().register(((IdentifierImpl)identifier).get(), ((ImplNativeImageBackedTexture)nativeImageBacketTexture).get());
    }

    @Override
    public INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException {
        return new ImplNativeImageBackedTexture(new DynamicTexture(NativeImage.read(inputStream)));
    }

    @Override
    public boolean isAbstractClientEntity(Object entity) {
        return entity instanceof AbstractClientPlayer && Minecraft.getInstance().player == entity; //make sure it'll work
    }

    @Override
    public void openScreen(IScreen screen) {
        Minecraft.getInstance().setScreen(((IScreen<Screen>)screen).getScreen());
    }

    @Override
    public IEmotePlayerEntity<EmotePlayImpl> getMainPlayer() {
        return (IEmotePlayerEntity) Minecraft.getInstance().player;
    }

    @Override
    public int getCurrentTick() {
        return tick;
    }

    @Override
    public boolean isPlayerBlocked(UUID uuid) {
        return Minecraft.getInstance().isBlocked(uuid);
    }

    @Override
    public int getPerspective() {
        return Minecraft.getInstance().options.getCameraType().ordinal();
    }

    @Override
    public void setPerspective(int p) {
        Minecraft.getInstance().options.setCameraType(CameraType.values()[p]);
    }

    @Override
    public void sendChatMessage(Text msg) {
        Minecraft.getInstance().gui.getChat().addMessage(((TextImpl)msg).get());
    }

    @Override
    public void toastExportMessage(int level, Text text, String msg) {
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.WORLD_BACKUP, ((TextImpl)text).get(), Component.literal(msg));
    }
}
