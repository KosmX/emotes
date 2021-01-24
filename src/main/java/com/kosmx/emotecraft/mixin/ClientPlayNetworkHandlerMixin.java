package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin implements IEmotecraftPresence {

    @Override
    public boolean hasEmotecraftInstalled() {
        return true; //TODO
    }

    @Override
    public void setEmotecraftInstalled(boolean bl) {

    }
}
