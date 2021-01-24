package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements IEmotecraftPresence {
    //Does the specific player have the mod installed
    boolean hasEmotecraft = true; //TODO

    @Override
    public boolean hasEmotecraftInstalled() {
        return this.hasEmotecraft;
    }

    @Override
    public void setEmotecraftInstalled(boolean bl) {
        this.hasEmotecraft = bl;
    }
}
