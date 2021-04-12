package io.github.kosmx.emotes.server;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * This will be used for modded servers
 *
 */
public abstract class AbstractServerEmotePlay<P> {

    protected boolean doValidate(){
        return false;
    }

    protected abstract UUID getUUIDFromPlayer(P player);

    public void receiveMessage(byte[] bytes, P player, INetworkInstance instance) throws IOException{
        receiveMessage(new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes)), player, instance);
    }

    public void receiveMessage(NetData data, P player, INetworkInstance instance) throws IOException {
        switch (data.purpose){
            case STOP:
                sendForEveryoneElse(data, player);
                break;
            case CONFIG:
                instance.setVersions(data.versions);
                break;
            case STREAM:
                streamEmote(data, player, instance);
                break;
            case UNKNOWN:
            default:
                throw new IOException("Unknown packet task");
        }
    }

    protected void streamEmote(NetData data, P player, INetworkInstance instance) throws IOException{
        if(!data.valid && doValidate()){
            EmotePacket.Builder stopMSG = new EmotePacket.Builder().configureToSendStop(data.emoteData.hashCode());
            instance.sendMessage(stopMSG, null);
        }
        else {
            UUID target = data.player;
            data.player = getUUIDFromPlayer(player);
            if(target == null) {
                sendForEveryoneElse(data, player);
            }else {
                sendForPlayer(data, player, target);
            }
        }
    }

    protected abstract void sendForEveryoneElse(NetData data, P player);

    protected abstract void sendForPlayer(NetData data, P player, UUID target);
}
