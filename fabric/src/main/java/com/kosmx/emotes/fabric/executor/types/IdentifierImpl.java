package com.kosmx.emotes.fabric.executor.types;

import com.kosmx.emotes.executor.dataTypes.IIdentifier;

public class IdentifierImpl implements IIdentifier {
    final net.minecraft.util.Identifier MCIdentifier;

    public IdentifierImpl(net.minecraft.util.Identifier mcIdentifier) {
        MCIdentifier = mcIdentifier;
    }

    public net.minecraft.util.Identifier get(){
        return MCIdentifier;
    }
}
