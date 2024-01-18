package io.github.kosmx.emotes.arch.executor.types;

import net.minecraft.resources.ResourceLocation;

public class IdentifierImpl {
    final ResourceLocation MCIdentifier;

    public IdentifierImpl(ResourceLocation mcIdentifier) {
        MCIdentifier = mcIdentifier;
    }

    public ResourceLocation get(){
        return MCIdentifier;
    }
}
