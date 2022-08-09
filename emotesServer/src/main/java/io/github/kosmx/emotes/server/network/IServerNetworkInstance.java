package io.github.kosmx.emotes.server.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.IOException;
import java.util.logging.Level;

public interface IServerNetworkInstance extends INetworkInstance {

    @Override
    default void presenceResponse() {
        INetworkInstance.super.presenceResponse();
        NetData configData = new EmotePacket.Builder().configureToConfigExchange(true).build().data;
        if (trackPlayState()) {
            configData.versions.put((byte)0x80, (byte)0x01);
        }
        try {
            this.sendMessage(new EmotePacket.Builder(configData), null);
        } catch(IOException e) {
            EmoteInstance.instance.getLogger().log(Level.SEVERE, e.getMessage());
        }
        if(this.getRemoteVersions().getOrDefault((byte)11, (byte)0) >= 0) {
            for (KeyframeAnimation emote : UniversalEmoteSerializer.serverEmotes.values()) {
                try{
                    this.sendMessage(new EmotePacket.Builder().configureToSaveEmote(emote).setSizeLimit(0x100000), null); //1 MB
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Server closes connection with instance
     */
    default void closeConnection(){}

    default boolean trackPlayState() {
        return true;
    }

    EmotePlayTracker getEmoteTracker();

}
