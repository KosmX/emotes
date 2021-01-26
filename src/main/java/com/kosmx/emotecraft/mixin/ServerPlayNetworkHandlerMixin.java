package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements IEmotecraftPresence {
    //Does the specific player have the mod installed
    int hasEmotecraft = 0;

    @Override
    public int getInstalledEmotecraft() {
        return hasEmotecraft;
    }

    @Override
    public void setInstalledEmotecraft(int ver) {
        hasEmotecraft = ver;
    }
}
