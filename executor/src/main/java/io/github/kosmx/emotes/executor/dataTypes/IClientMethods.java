package io.github.kosmx.emotes.executor.dataTypes;

import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.io.IOException;
import java.io.InputStream;

public interface IClientMethods {
    void destroyTexture(IIdentifier identifier);
    void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture);

    INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException;

    boolean isAbstractClientEntity(Object entity);

    void openScreen(IScreen screen);

    IEmotePlayerEntity getMainPlayer();

    INetworkInstance getServerNetworkController();

    int getCurrentTick();

}
