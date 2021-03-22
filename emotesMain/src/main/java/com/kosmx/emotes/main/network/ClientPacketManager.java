package com.kosmx.emotes.main.network;

import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.common.network.objects.NetData;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/**
 * //TODO
 */
public class ClientPacketManager {

    private final static ArrayList<IClientNetwork> networkInstances = new ArrayList<>();
    private static final IClientNetwork defaultNetwork = (IClientNetwork) EmoteInstance.instance.getClientMethods().getServerNetworkController();
    //that casting should always work

    private ClientPacketManager(){} //that is a utility class :D

    /**
     *
     * @return Use all network instances even if the server has the mod installed
     */
    private static boolean useAlwaysAlt(){
        return false;
    }

    static void send(EmotePacket.Builder packetBuilder, IEmotePlayerEntity target){
        if(!defaultNetwork.isActive() || useAlwaysAlt()){
            for(IClientNetwork network:networkInstances){
                if(network.isActive()){
                    try {
                        EmotePacket.Builder builder = packetBuilder.copy();
                        if(!network.sendPlayerID())builder.removePlayerID();
                        network.sendMessage(builder, target);    //everything is happening on the heap, there won't be any memory leak
                    } catch (IOException exception) {
                        EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while sending packet: " + exception.getMessage(), true);
                        if(EmoteInstance.config.showDebug) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }
        if(defaultNetwork.isActive()){
            if(!defaultNetwork.sendPlayerID())packetBuilder.removePlayerID();
            try {
                defaultNetwork.sendMessage(packetBuilder.build().write(), target);
            }
            catch (IOException exception){
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while sending packet: " + exception.getMessage(), true);
                if(EmoteInstance.config.showDebug) {
                    exception.printStackTrace();
                }
            }
        }
    }

    static void receiveMessage(ByteBuffer buffer, UUID player, IClientNetwork networkManager){
        try{
            NetData data = new EmotePacket.Builder().build().read(buffer);
            if(data == null){
                throw new IOException("no valid data");
            }
            if(player != null) {
                data.player = player;
            }
            if(data.player == null){
                //this is not exactly IO but something went wrong in IO so it is IO fail
                throw new IOException("Didn't received any player information");
            }

            try {
                ClientEmotePlay.executeMessage(data, networkManager);
            }
            catch (Exception e){//I don't want to break the whole game with a bad message but I'll warn with the highest level
                EmoteInstance.instance.getLogger().log(Level.SEVERE, "Critical error has occurred while receiving emote: " + e.getMessage(), true);
                e.printStackTrace();

            }

        }
        catch (IOException e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while receiving packet: " + e.getMessage(), true);
            if(EmoteInstance.config.showDebug) {
                e.printStackTrace();
            }
        }
    }

}
