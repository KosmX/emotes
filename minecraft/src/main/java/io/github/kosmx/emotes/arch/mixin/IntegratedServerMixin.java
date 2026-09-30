package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import io.github.kosmx.emotes.arch.online.OnlineNetworkInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Online Emotes runs in singleplayer only while the world is open to LAN. Called on the server thread. */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z", at = @At("RETURN"))
    private void emotecraft$onPublishServer(MinecraftServer.MultiplayerScope scope, int port, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) Minecraft.getInstance().execute(OnlineEmotes::connect);
    }

    @Inject(method = "unpublishServer()Z", at = @At("RETURN"))
    private void emotecraft$onUnpublishServer(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) Minecraft.getInstance().execute(OnlineNetworkInstance.INSTANCE::disconnect);
    }
}
