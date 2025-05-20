package io.github.kosmx.emotes.main.network;

import dev.kosmx.playerAnim.core.impl.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientNetworkEvents;
import io.github.kosmx.emotes.api.proxy.EmotesProxyManager;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Client emote proxy manager
 * Responsible for calling proxy instances and other stuff
 */
public final class ClientPacketManager extends EmotesProxyManager {
    private static final INetworkInstance DEFAULT_NETWORK = PlatformTools.getClientNetworkController();

    public static void init() {
        setManager(new ClientPacketManager()); //Some dependency injection
    }

    private ClientPacketManager() {} //that is a utility class :D

    public static void send(EmotePacket.Builder packetBuilder, UUID target) {
        if (ClientNetworkEvents.PACKET_SEND.invoker().onPacketSend(packetBuilder) == EventResult.FAIL) {
            LoggerService.INSTANCE.log(Level.INFO, "Sending the packet has been canceled by the event!");
            return; // Deny
        }

        INetworkInstance instance = getActiveNetworkInstance();
        if (instance != null && instance.isActive()) {
            if (!instance.sendPlayerID()) packetBuilder.removePlayerID();

            try {
                packetBuilder.setSizeLimit(instance.maxDataSize(), false);
                packetBuilder.setVersion(instance.getRemoteVersions());
                instance.sendMessage(packetBuilder, target);
            } catch (IOException exception) {
                LoggerService.INSTANCE.log(Level.WARNING, "Error while sending packet!", exception);
            }
        }
    }

    static void receiveMessage(ByteBuffer buffer, UUID player, INetworkInstance networkInstance) {
        try{
            NetData data = new EmotePacket.Builder().setThreshold(PlatformTools.getConfig().validThreshold.get()).build().read(buffer);
            if (!networkInstance.trustReceivedPlayer()) data.player = null;
            if (player != null) data.player = player;

            if (data.player == null && data.purpose.playerBound) {
                //this is not exactly IO but something went wrong in IO so it is IO fail
                throw new IOException("Didn't received any player information");
            }

            try {
                ClientEmotePlay.executeMessage(data, networkInstance);
            } catch (Exception e) { // I don't want to break the whole game with a bad message but I'll warn with the highest level
                LoggerService.INSTANCE.log(Level.SEVERE, "Critical error has occurred while receiving emote!", e);
            }
        }
        catch (IOException e){
            LoggerService.INSTANCE.log(Level.WARNING, "Error while receiving packet!", e);
        }
    }

    @Override
    protected void dispatchReceive(ByteBuffer buffer, UUID player, INetworkInstance networkInstance) {
        receiveMessage(buffer, player, networkInstance);
    }

    public static boolean isRemoteSupportSync() {
        INetworkInstance instance = getActiveNetworkInstance();
        return instance != null && instance.getRemoteVersions().containsKey(PacketConfig.TIME_DATA_PACKET);
    }

    public static boolean isDefaultRemoteAvailable() {
        return DEFAULT_NETWORK.isActive();
    }

    public static boolean isRemoteTracking() {
        INetworkInstance instance = getActiveNetworkInstance();
        return instance != null && instance.isServerTrackingPlayState();
    }

    public static @Nullable INetworkInstance getActiveNetworkInstance() {
        if (isDefaultRemoteAvailable()) return DEFAULT_NETWORK;
        return getAvailableProxyInstance();
    }

    public static boolean isAvailableProxy() {
        return getAvailableProxyInstance() != null;
    }

    public static @Nullable INetworkInstance getAvailableProxyInstance() {
        for (INetworkInstance instance : networkInstances) {
            if (instance.isActive()) return instance;
        }
        return null;
    }

    /**
     * This shall be invoked when disconnecting from the server
     * @param networkInstance ...
     */
    @Override
    public void onDisconnectFromServer(INetworkInstance networkInstance){
        if(networkInstance == null)throw new NullPointerException("network instance must be non-null");
        EmoteHolder.list.removeIf(emoteHolder -> emoteHolder.fromInstance == networkInstance);
    }
}
