package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.fabric.emote.EmotePlayImpl;
import io.github.kosmx.emotes.fabric.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.fabric.executor.types.ImplNativeImageBackedTexture;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unchecked")
public class FabricClientMethods implements IClientMethods {
    public static int tick = 0;

    @Override
    public void destroyTexture(IIdentifier identifier) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(((IdentifierImpl)identifier).get());
    }

    @Override
    public void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture) {
        MinecraftClient.getInstance().getTextureManager().registerTexture(((IdentifierImpl)identifier).get(), ((ImplNativeImageBackedTexture)nativeImageBacketTexture).get());
    }

    @Override
    public INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException {
        return new ImplNativeImageBackedTexture(new NativeImageBackedTexture(NativeImage.read(inputStream)));
    }

    @Override
    public boolean isAbstractClientEntity(Object entity) {
        return entity instanceof AbstractClientPlayerEntity && MinecraftClient.getInstance().player == entity; //make sure it'll work
    }

    @Override
    public void openScreen(IScreen screen) {
        MinecraftClient.getInstance().openScreen(((IScreen<Screen>)screen).getScreen());
    }

    @Override
    public IEmotePlayerEntity<EmotePlayImpl> getMainPlayer() {
        return (IEmotePlayerEntity) MinecraftClient.getInstance().player;
    }

    @Override
    public INetworkInstance getServerNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }

    @Override
    public int getCurrentTick() {
        return tick;
    }

}
