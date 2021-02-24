package com.kosmx.emotes.fabric.executor;

import com.kosmx.emotes.executor.INetworkInstance;
import com.kosmx.emotes.executor.dataTypes.IClientMethods;
import com.kosmx.emotes.executor.dataTypes.IIdentifier;
import com.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import com.kosmx.emotes.executor.dataTypes.screen.IScreen;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.fabric.emote.EmotePlayImpl;
import com.kosmx.emotes.fabric.executor.types.IdentifierImpl;
import com.kosmx.emotes.fabric.executor.types.ImplNativeImageBackedTexture;
import com.kosmx.emotes.fabric.network.ClientNetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.IOException;
import java.io.InputStream;

public class FabricClientMethods implements IClientMethods {
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
        return new ClientNetworkManager();//TODO Not do this junk...
    }

}
