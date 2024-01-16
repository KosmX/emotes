package io.github.kosmx.emotes.arch.executor.types;

import io.github.kosmx.emotes.inline.dataTypes.IIdentifier;

public class IdentifierImpl implements IIdentifier {
    final net.minecraft.resources.ResourceLocation MCIdentifier;

    public IdentifierImpl(net.minecraft.resources.ResourceLocation mcIdentifier) {
        MCIdentifier = mcIdentifier;
    }

    public net.minecraft.resources.ResourceLocation get(){
        return MCIdentifier;
    }
}
