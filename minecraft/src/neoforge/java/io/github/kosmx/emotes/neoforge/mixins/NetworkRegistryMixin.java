package io.github.kosmx.emotes.neoforge.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.emotes.arch.network.EmotePacketPayload;
import io.github.kosmx.emotes.arch.network.NetworkPlatformTools;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {
    @WrapOperation(
            method = "getCodec",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/network/registration/PayloadRegistration;codec()Lnet/minecraft/network/codec/StreamCodec;"
            )
    )
    private static StreamCodec<?, ?> emotecraft$fix(PayloadRegistration<?> instance, Operation<StreamCodec<?, ?>> original, @Local(argsOnly = true) PacketFlow flow) {
        StreamCodec<?, ?> codec = original.call(instance);
        if (codec != null) return codec;

        if (instance.type().equals(NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
            return flow.isServerbound() ? EmotePacketPayload.EMOTE_CHANNEL_READER_C2S : EmotePacketPayload.EMOTE_CHANNEL_READER_S2C;
        } else if (instance.type().equals(NetworkPlatformTools.STREAM_CHANNEL_ID)) {
            return flow.isServerbound() ? EmotePacketPayload.STREAM_CHANNEL_READER_C2S : EmotePacketPayload.STREAM_CHANNEL_READER_S2C;
        }

        return null;
    }
}
