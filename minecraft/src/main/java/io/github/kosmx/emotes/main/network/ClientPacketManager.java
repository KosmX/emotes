package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientNetworkEvents;
import io.github.kosmx.emotes.api.proxy.EmotesProxyManager;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.main.EmoteHolder;

import java.io.IOException;
import java.util.UUID;

/**
 * Client emote proxy manager
 * Responsible for calling proxy instances and other stuff
 */
public final class ClientPacketManager extends EmotesProxyManager {

    private static final INetworkInstance defaultNetwork = PlatformTools.getClientNetworkController();
    //that casting should always work

    public static void init(){
        setManager(new ClientPacketManager()); //Some dependency injection
    }

    private ClientPacketManager(){} //that is a utility class :D

    /**
     *
     * @return Use all network instances even if the server has the mod installed
     */
    private static boolean useAlwaysAlt(){
        return false;
    }

    public static void send(EmotePacket.Builder packetBuilder, UUID target){
        ClientNetworkEvents.PACKET_SEND.invoker().onPacketSend(packetBuilder);
        if(!defaultNetwork.isActive() || useAlwaysAlt()){
            for(INetworkInstance network:networkInstances){
                if(network.isActive()){
                    if (target == null || !network.isServerTrackingPlayState()) {
                        try {
                            EmotePacket.Builder builder = packetBuilder.copy();
                            if (!network.sendPlayerID()) builder.removePlayerID();
                            builder.setSizeLimit(network.maxDataSize(), false);
                            builder.setVersion(network.getRemoteVersions());
                            network.sendMessage(builder, target);    //everything is happening on the heap, there won't be any memory leak
                        } catch(IOException exception) {
                            CommonData.LOGGER.error("Error while sending packet!", exception);
                        }
                    }
                }
            }
        }
        if(defaultNetwork.isActive() && (target == null || !defaultNetwork.isServerTrackingPlayState())){
            if(!defaultNetwork.sendPlayerID())packetBuilder.removePlayerID();
            try {
                packetBuilder.setSizeLimit(defaultNetwork.maxDataSize(), false);
                packetBuilder.setVersion(defaultNetwork.getRemoteVersions());
                defaultNetwork.sendMessage(packetBuilder, target);
            }
            catch (IOException exception){
                CommonData.LOGGER.error("Error while sending packet!", exception);
            }
        }
    }

    @Override
    protected void dispatchReceive(EmotePacket packet, UUID player, INetworkInstance networkInstance) {
        try {
            if(!networkInstance.trustReceivedPlayer()){
                packet.data.player = null;
            }
            if(player != null) {
                packet.data.player = player;
            }
            if(packet.data.player == null && packet.data.purpose.playerBound){
                //this is not exactly IO but something went wrong in IO so it is IO fail
                throw new IOException("Didn't received any player information");
            }

            try {
                ClientEmotePlay.executeMessage(packet.data, networkInstance);
            } catch (Exception e) {//I don't want to break the whole game with a bad message but I'll warn with the highest level
                CommonData.LOGGER.error("Critical error has occurred while receiving emote!", e);
            }
        } catch (IOException e) {
            CommonData.LOGGER.warn("Error while receiving packet!", e);
        }
    }

    public static boolean isRemoteAvailable(){
        return defaultNetwork.isActive();
    }

    public static boolean isRemoteTracking() {
        return isRemoteAvailable() && defaultNetwork.isServerTrackingPlayState();
    }

    public static boolean isAvailableProxy(){
        for(INetworkInstance instance : networkInstances){
            if(instance.isActive()){
                return true;
            }
        }
        return false;
    }

    /**
     * This shall be invoked when disconnecting from the server
     * @param networkInstance ...
     */
    @Override
    public void onDisconnectFromServer(INetworkInstance networkInstance) {
        if (networkInstance == null) throw new NullPointerException("network instance must be non-null");
        EmoteHolder.clearEmotes(networkInstance);
    }
}
