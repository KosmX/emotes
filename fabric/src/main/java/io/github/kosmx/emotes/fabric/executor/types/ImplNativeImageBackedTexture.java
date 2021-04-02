package io.github.kosmx.emotes.fabric.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class ImplNativeImageBackedTexture implements INativeImageBacketTexture {

    final NativeImageBackedTexture nibt;

    public ImplNativeImageBackedTexture(NativeImageBackedTexture nibt) {
        this.nibt = nibt;
    }

    public NativeImageBackedTexture get() {
        return nibt;
    }

    @Override
    public void close() {
        nibt.close();
    }
}
