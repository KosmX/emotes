package io.github.kosmx.emotes.main.mixinFunctions;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import java.util.concurrent.atomic.AtomicInteger;


public interface IPlayerEntity<T> extends IEmotePlayerEntity<EmotePlayer<T>> {
    AtomicInteger ticked = new AtomicInteger(0);

    int FPPerspective = 0;
    int TPBPerspective = 1;

    default void initEmotePlay(){

        Pair<EmoteData, Integer> p = ClientEmotePlay.getEmoteForUUID(this.emotes_getUUID());
        if(p != null){
            this.playEmote(p.getLeft(), p.getRight());
        }
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
        if(ticked.get() >= 3){ //Emote init with a little delay (60-80 ms)
            initEmotePlay();
        }
        else ticked.getAndIncrement();

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
