package io.github.kosmx.emotes.main.mixinFunctions;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

public interface IPlayerEntity<T> extends IEmotePlayerEntity<EmotePlayer<T>> {

    @Override
    default void init(){
        Pair<EmoteData, Integer> p = ClientEmotePlay.getEmoteForUUID(this.getUUID());
        if(p != null){
            this.playEmote(p.getLeft(), p.getRight());
        }
        if(EmoteInstance.instance.getClientMethods().getMainPlayer() != null && EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()){
            IEmotePlayerEntity playerEntity = EmoteInstance.instance.getClientMethods().getMainPlayer();
            ClientEmotePlay.clientRepeateLocalEmote(playerEntity.getEmote().getData(), playerEntity.getEmote().getTick(), this);
        }

    }

    @Override
    default boolean isPlayingEmote(){
        return EmotePlayer.isRunningEmote(this.getEmote());
    }

    @Override
    default boolean isMainPlayer(){
        return EmoteInstance.instance.getClientMethods().getMainPlayer() == this;
    }

    @Override
    default void emoteTick(){
        if(isPlayingEmote()){
            setBodyYaw((getBodyYaw() * 3 + getViewYaw()) / 4);
            emoteTickCallback();
            if(!this.isMainPlayer() || EmoteHolder.canRunEmote(this)){
                this.getEmote().tick();
            }
            else {
                this.getEmote().stop();
                ClientEmotePlay.clientStopLocalEmote(this.getEmote().getData());
            }
        }
    }

    @Override
    default void stopEmote(){
        if(getEmote() != null) {
            this.getEmote().stop();
        }
        this.voidEmote();
    }

    @Override
    default void stopEmote(int emoteID){
        if(getEmote() != null && getEmote().getData().hashCode() == emoteID){
            this.getEmote().stop();
        }
        this.voidEmote();
    }

    void voidEmote();
}
