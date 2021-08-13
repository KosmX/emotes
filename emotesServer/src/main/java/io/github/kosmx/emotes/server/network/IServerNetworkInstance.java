package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.IOException;

public interface IServerNetworkInstance extends INetworkInstance {
    @Override
    default void presenceResponse() {
        INetworkInstance.super.presenceResponse();
        if(this.getVersions().getOrDefault((byte)11, (byte)0) >= 0) {
            for (EmoteData emote : UniversalEmoteSerializer.serverEmotes) {
                try{
                    this.sendMessage(new EmotePacket.Builder().configureToSaveEmote(emote).setSizeLimit(0x100000), null); //1 MB
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
