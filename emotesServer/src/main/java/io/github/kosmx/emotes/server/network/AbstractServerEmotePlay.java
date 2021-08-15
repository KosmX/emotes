package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * This will be used for modded servers
 *
 */
public abstract class AbstractServerEmotePlay<P> {

    protected boolean doValidate(){
        return EmoteInstance.config.validateEmote.get();
    }

    protected abstract UUID getUUIDFromPlayer(P player);

    public void receiveMessage(byte[] bytes, P player, INetworkInstance instance) throws IOException{
        receiveMessage(new EmotePacket.Builder().setThreshold(EmoteInstance.config.validThreshold.get()).build().read(ByteBuffer.wrap(bytes)), player, instance);
    }

    public void receiveMessage(NetData data, P player, INetworkInstance instance) throws IOException {
        switch (data.purpose){
            case STOP:
                sendForEveryoneElse(data, player);
                break;
            case CONFIG:
                instance.setVersions(data.versions);
                instance.presenceResponse();
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
            EmotePacket.Builder stopMSG = new EmotePacket.Builder().configureToSendStop(data.emoteData.hashCode()).configureTarget(getUUIDFromPlayer(player));
            instance.sendMessage(stopMSG, null);
        }
        else {
            UUID target = data.player;
            data.player = getUUIDFromPlayer(player);
            if(target == null) {
                sendForEveryoneElse(data, player);
            }else {
                sendForPlayerInRange(data, player, target);
            }
        }
    }

    /**
     * Send the message to everyone, except for the player
     * @param data message
     * @param player send around this player
     */
    protected abstract void sendForEveryoneElse(NetData data, P player);

    /**
     * Send message to target. If target see player the message will be sent
     * @param data message
     * @param player around player
     * @param target target player
     */
    protected abstract void sendForPlayerInRange(NetData data, P player, UUID target);

    /**
     * Send a message to target. This will send a message even if target doesn't see player
     * @param data message
     * @param player player for the ServerWorld information
     * @param target target entity
     */
    protected abstract void sendForPlayer(NetData data, P player, UUID target);
}
