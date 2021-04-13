package io.github.kosmx.emotes.main.mixinFunctions;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IPlayerEntity<T> extends IEmotePlayerEntity<EmotePlayer<T>> {
    AtomicBoolean ticked = new AtomicBoolean(false);

    int FPPerspective = 0;
    int TPBPerspective = 1;

    @Override
    default void init(){
        if(EmoteInstance.instance.getClientMethods().getMainPlayer() != null && EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()){
            IEmotePlayerEntity playerEntity = EmoteInstance.instance.getClientMethods().getMainPlayer();
            ClientEmotePlay.clientRepeateLocalEmote(playerEntity.getEmote().getData(), playerEntity.getEmote().getTick(), this.emotes_getUUID());
        }

    }

    default void initEmotePerspective(EmotePlayer emotePlayer){
        if(((ClientConfig)EmoteInstance.config).enablePerspective.get() && isMainPlayer() && EmoteInstance.instance.getClientMethods().getPerspective() == FPPerspective) {
            emotePlayer.perspective = 1;
            EmoteInstance.instance.getClientMethods().setPerspective(TPBPerspective);
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
        if(!ticked.get()){ //to solve the out-of sync issue
            ticked.set(true);
            Pair<EmoteData, Integer> p = ClientEmotePlay.getEmoteForUUID(this.emotes_getUUID());
            if(p != null){
                this.playEmote(p.getLeft(), p.getRight());
            }
        }
        if(isPlayingEmote()){
            setBodyYaw((getBodyYaw() * 3 + getViewYaw()) / 4);
            emoteTickCallback();
            if(this.isMainPlayer() && getEmote().perspective == 1 && EmoteInstance.instance.getClientMethods().getPerspective() != TPBPerspective){
                this.getEmote().perspective = 0;
            }
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
            this.voidEmote();
        }
    }

    @Override
    default void stopEmote(int emoteID){
        if(getEmote() != null && getEmote().getData().hashCode() == emoteID){
            this.getEmote().stop();
            this.voidEmote();
        }
    }

    void voidEmote();
}
