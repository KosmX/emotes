package io.github.kosmx.emotes.arch.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.INativeImageBacketTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class ImplNativeImageBackedTexture implements INativeImageBacketTexture {

    final DynamicTexture nibt;

    public ImplNativeImageBackedTexture(DynamicTexture nibt) {
        this.nibt = nibt;
    }

    public DynamicTexture get() {
        return nibt;
    }

    @Override
    public void close() {
        nibt.close();
    }
}
