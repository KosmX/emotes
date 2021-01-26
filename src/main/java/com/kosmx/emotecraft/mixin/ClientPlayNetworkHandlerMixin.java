package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin implements IEmotecraftPresence {

    int emotecraftVer = 0;

    @Override
    public int getInstalledEmotecraft() {
        return emotecraftVer;
    }

    @Override
    public void setInstalledEmotecraft(int ver) {
        emotecraftVer = ver;
    }
}
