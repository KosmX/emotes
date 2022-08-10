package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.api.proxy.EmotesProxyManager;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Client emote proxy manager
 * Responsible for calling proxy instances and other stuff
 */
public final class ClientPacketManager extends EmotesProxyManager {

    private static final INetworkInstance defaultNetwork = EmoteInstance.instance.getClientMethods().getServerNetworkController();
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

    static void send(EmotePacket.Builder packetBuilder, UUID target){
        if(!defaultNetwork.isActive() || useAlwaysAlt()){
            for(INetworkInstance network:networkInstances){
                if(network.isActive()){
                    if (target == null || !network.isServerTrackingPlayState()) {
                        try {
                            EmotePacket.Builder builder = packetBuilder.copy();
                            if (!network.sendPlayerID()) builder.removePlayerID();
                            builder.setSizeLimit(network.maxDataSize());
                            builder.setVersion(network.getRemoteVersions());
                            network.sendMessage(builder, target);    //everything is happening on the heap, there won't be any memory leak
                        } catch(IOException exception) {
                            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while sending packet: " + exception.getMessage(), true);
                            if (EmoteInstance.config.showDebug.get()) {
                                exception.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if(defaultNetwork.isActive() && (target == null || !defaultNetwork.isServerTrackingPlayState())){
            if(!defaultNetwork.sendPlayerID())packetBuilder.removePlayerID();
            try {
                packetBuilder.setSizeLimit(defaultNetwork.maxDataSize());
                packetBuilder.setVersion(defaultNetwork.getRemoteVersions());
                defaultNetwork.sendMessage(packetBuilder, target);
            }
            catch (IOException exception){
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while sending packet: " + exception.getMessage(), true);
                if(EmoteInstance.config.showDebug.get()) {
                    exception.printStackTrace();
                }
            }
        }
    }

    static void receiveMessage(ByteBuffer buffer, UUID player, INetworkInstance networkInstance){
        try{
            NetData data = new EmotePacket.Builder().setThreshold(EmoteInstance.config.validThreshold.get()).build().read(buffer);
            if(data == null){
                throw new IOException("no valid data");
            }
            if(!networkInstance.trustReceivedPlayer()){
                data.player = null;
            }
            if(player != null) {
                data.player = player;
            }
            if(data.player == null && data.purpose.playerBound){
                //this is not exactly IO but something went wrong in IO so it is IO fail
                throw new IOException("Didn't received any player information");
            }

            try {
                ClientEmotePlay.executeMessage(data, networkInstance);
            }
            catch (Exception e){//I don't want to break the whole game with a bad message but I'll warn with the highest level
                EmoteInstance.instance.getLogger().log(Level.SEVERE, "Critical error has occurred while receiving emote: " + e.getMessage(), true);
                e.printStackTrace();

            }

        }
        catch (IOException e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while receiving packet: " + e.getMessage(), true);
            if(EmoteInstance.config.showDebug.get()) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void logMSG(Level level, String msg) {
        EmoteInstance.instance.getLogger().log(level, "[emotes proxy module] " +  msg, level.intValue() >= Level.WARNING.intValue());
    }

    @Override
    protected void dispatchReceive(ByteBuffer buffer, UUID player, INetworkInstance networkInstance) {
        receiveMessage(buffer, player, networkInstance);
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
    public void onDisconnectFromServer(INetworkInstance networkInstance){
        if(networkInstance == null)throw new NullPointerException("network instance must be non-null");
        EmoteHolder.list.removeIf(emoteHolder -> emoteHolder.fromInstance == networkInstance);
    }
}
