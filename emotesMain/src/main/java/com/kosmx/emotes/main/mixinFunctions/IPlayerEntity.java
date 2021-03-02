package com.kosmx.emotes.main.mixinFunctions;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.emotePlay.EmotePlayer;
import com.kosmx.emotes.main.network.ClientEmotePlay;

public interface IPlayerEntity<T> extends IEmotePlayerEntity<EmotePlayer<T>> {
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
        this.getEmote().stop();
        this.voidEmote();
    }

    void voidEmote();
}