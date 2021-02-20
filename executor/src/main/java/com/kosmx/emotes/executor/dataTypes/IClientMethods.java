package com.kosmx.emotes.executor.dataTypes;

import java.io.IOException;
import java.io.InputStream;

public interface IClientMethods {
    void destroyTexture(IIdentifier identifier);
    void registerTexture(IIdentifier identifier, INativeImageBacketTexture nativeImageBacketTexture);
    InputStream getResourceAsStream(String str);
    INativeImageBacketTexture readNativeImage(InputStream inputStream) throws IOException;

    boolean isAbstractClientEntity(Object entity);

}
