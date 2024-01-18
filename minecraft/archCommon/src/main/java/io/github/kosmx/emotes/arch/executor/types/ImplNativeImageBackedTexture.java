package io.github.kosmx.emotes.arch.executor.types;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class ImplNativeImageBackedTexture {

    final DynamicTexture nibt;

    public ImplNativeImageBackedTexture(DynamicTexture nibt) {
        this.nibt = nibt;
    }

    public DynamicTexture get() {
        return nibt;
    }

    public void close() {
        nibt.close();
    }
}
