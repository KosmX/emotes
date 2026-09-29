package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    // Others can join now, so Online Emotes has someone to stream to. The /publish command runs on the server thread.
    @Inject(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z", at = @At("RETURN"))
    private void emotecraft$onPublishServer(MinecraftServer.MultiplayerScope scope, int port, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) Minecraft.getInstance().execute(OnlineEmotes::connect);
    }
}
