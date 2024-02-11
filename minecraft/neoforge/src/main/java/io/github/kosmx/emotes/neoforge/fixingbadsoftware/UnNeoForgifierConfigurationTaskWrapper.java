package io.github.kosmx.emotes.neoforge.fixingbadsoftware;

import io.github.kosmx.emotes.neoforge.ForgeWrapper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class UnNeoForgifierConfigurationTaskWrapper implements ICustomConfigurationTask {
    @NotNull public static ICustomConfigurationTask wrap(@NotNull ConfigurationTask task){
        return new UnNeoForgifierConfigurationTaskWrapper(task);
    }

    @NotNull
    private final ConfigurationTask originalTask;

    public UnNeoForgifierConfigurationTaskWrapper(@NotNull ConfigurationTask originalTask) {
        this.originalTask = originalTask;
    }

    @Override
    public void run(@NotNull Consumer<CustomPacketPayload> consumer) {
        ForgeWrapper.logger.warn("NeoForge is invoking a method that shouldn't be invoked. Consider using Fabric");
        originalTask.start(packet -> {
            if (packet instanceof ClientboundCustomPayloadPacket clientPacket) {
                consumer.accept(clientPacket.payload()); // and let it re-wrap into the same class
            } else {
                throw new AssertionError("Wrapped task does not use CustomPayloadPacket and NeoForge does not respect MC API");
            }
        });
    }

    @Override
    @SuppressWarnings("ALL") // Fuck you NeoForge for this. This method should not be internal and your API should accept the base ConfigureTask
    public void start(@NotNull Consumer<Packet<?>> sender) {
        originalTask.start(sender);
    }

    @Override
    public @NotNull Type type() {
        return originalTask.type();
    }
}
