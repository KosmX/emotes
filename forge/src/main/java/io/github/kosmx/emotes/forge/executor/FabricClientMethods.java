package io.github.kosmx.emotes.forge.executor;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.forge.emote.EmotePlayImpl;
import io.github.kosmx.emotes.forge.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.forge.executor.types.ImplNativeImageBackedTexture;
import io.github.kosmx.emotes.forge.network.ClientNetworkInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unchecked")
public class FabricClientMethods implements IClientMethods {
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
    public INetworkInstance getServerNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }

    @Override
    public int getCurrentTick() {
        return tick;
    }

}
