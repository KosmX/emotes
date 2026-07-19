package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.api.events.client.ClientNetworkEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Client emote proxy manager
 * Responsible for calling proxy instances and other stuff
 */
@SuppressWarnings("unused") // API
public final class ClientPacketManager {
    /**
     * The list of registered instances.
     * To register yours use {@link #registerProxyInstance(INetworkInstance)}
     */
    private final static ArrayList<INetworkInstance> NETWORK_INSTANCES = new ArrayList<>(1);

    private ClientPacketManager() {
        // that is a utility class :D
    }

    /**
     * Register your proxy instance
     * use {@link INetworkInstance} to create a new instance
     * @param instance your instance
     * @return true if registered {@link ArrayList#add(Object)}
     */
    public static boolean registerProxyInstance(INetworkInstance instance) {
        if (!NETWORK_INSTANCES.contains(instance)) return NETWORK_INSTANCES.add(instance);
        return false;
    }

    /**
     * Unregister your proxy instance
     * Why were you doing that, you can use {@link INetworkInstance#isActive()} to temporally disable it
     * @param instance instance to unregister
     * @return if it was unregistered {@link ArrayList#remove(Object)}
     */
    public static boolean unregisterProxyInstance(INetworkInstance instance) {
        return NETWORK_INSTANCES.remove(instance);
    }

    public static void send(EmotePacket.Builder packetBuilder, UUID target) {
        ClientNetworkEvents.PACKET_SEND.invoker().onPacketSend(packetBuilder);

        boolean isMainActive = ClientNetwork.INSTANCE.isActive();
        if (isMainActive) { // Always try to send to main
            ClientPacketManager.sendMessageVia(ClientNetwork.INSTANCE, packetBuilder, target);
        }

        if (!isMainActive || isInstanceOutdatedForStreaming(ClientNetwork.INSTANCE)) {
            for (INetworkInstance network : NETWORK_INSTANCES) {
                if (!network.isActive()) continue;
                ClientPacketManager.sendMessageVia(network, packetBuilder.copy(), target);
            }
        }
    }

    private static void sendMessageVia(INetworkInstance network, EmotePacket.Builder packetBuilder, UUID target) {
        if (target != null && network.isTrackingPlayState()) return;

        if (network.isTrackingPlayState()) packetBuilder.removePlayerID();
        try {
            if (target != null) packetBuilder.configureTarget(target);
            packetBuilder.setSizeLimit(network.maxDataSize(), false);
            network.sendMessage(packetBuilder, true);
        } catch (Exception ex) {
            CommonData.LOGGER.error("Error while sending packet via {}!", network, ex);
        }
    }

    public static boolean isInstanceOutdatedForStreaming(INetworkInstance instance) {
        return isInstanceOutdated(instance, PacketConfig.NEW_ANIMATION_FORMAT) ||
                isInstanceOutdated(instance, PacketConfig.NBS_CONFIG);
    }

    public static boolean isInstanceOutdated(INetworkInstance instance, byte packet) {
        Map<Byte, Byte> versions = instance.getVersions();
        if (!versions.containsKey(packet)) return true;
        return versions.get(packet) < EmotePacket.defaultVersions.get(packet);
    }

    public static boolean isAvailableProxy() {
        for (INetworkInstance instance : NETWORK_INSTANCES) {
            if (instance.isActive()) return true;
        }
        return false;
    }
}
