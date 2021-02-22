package com.kosmx.emotes.executor.dataTypes;

import com.kosmx.emotes.executor.dataTypes.screen.IScreen;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.io.IOException;
import java.io.InputStream;

public interface IClientMethods {
    void destroyTexture(IIdentifier identifier);
    void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture);
    InputStream getResourceAsStream(String str);
    INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException;

    boolean isAbstractClientEntity(Object entity);

    void openScreen(IScreen screen);

    IEmotePlayerEntity getMainPlayer();

}
