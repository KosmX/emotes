package com.kosmx.emotes.main.network;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.INetworkInstance;
import com.kosmx.emotes.main.EmoteHolder;

import java.io.IOException;

public class ClientEmotePlay {
    public static void clientStartLocalEmote(EmoteHolder emoteHolder){
        clientStartLocalEmote(emoteHolder.getEmote());
    }
    public static boolean clientStartLocalEmote(EmoteData emote){
        INetworkInstance networkInstance = EmoteInstance.instance.getClientMethods().getServerNetworkController();
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder(networkInstance.getVersions());
        packetBuilder.configureToSendEmote(emote, networkInstance.sendPlayerData() ? EmoteInstance.instance.getClientMethods().getMainPlayer().getUUID() : null);
        try {
            networkInstance.sendByteArray(packetBuilder.build().write().array());
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
        EmoteInstance.instance.getClientMethods().getMainPlayer().playEmote(emote);
        return true;
    }

    public static void clientStopLocalEmote(){
        if(EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()) {
            clientStopLocalEmote(EmoteInstance.instance.getClientMethods().getMainPlayer().getEmote().getData());
            EmoteInstance.instance.getClientMethods().getMainPlayer().stopEmote();
        }
    }

    public static void clientStopLocalEmote(EmoteData emoteData){

    }
}
