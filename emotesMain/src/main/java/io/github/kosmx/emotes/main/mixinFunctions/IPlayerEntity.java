package io.github.kosmx.emotes.main.mixinFunctions;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;

import java.util.UUID;
import java.util.function.Supplier;


public interface IPlayerEntity<T> extends IEmotePlayerEntity<EmotePlayer<T>> {

    int FPPerspective = 0;
    Supplier<Integer> TPBPerspective = () -> (((ClientConfig)EmoteInstance.config).frontAsTPPerspective.get() ? 2 : 1);

    default void initEmotePlay(){

        Pair<EmoteData, Integer> p = ClientEmotePlay.getEmoteForUUID(this.emotes_getUUID());
        if(p != null){
            this.playEmote(p.getLeft(), p.getRight(), false);
        }
        if(!this.isMainPlayer() && EmoteInstance.instance.getClientMethods().getMainPlayer() != null && EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()){
            IEmotePlayerEntity playerEntity = EmoteInstance.instance.getClientMethods().getMainPlayer();
            ClientEmotePlay.clientRepeatLocalEmote(playerEntity.getEmote().getData(), playerEntity.getEmote().getTick(), this.emotes_getUUID());
        }

    }

    default void initEmotePerspective(EmotePlayer emotePlayer){
        if(((ClientConfig)EmoteInstance.config).enablePerspective.get() && isMainPlayer() && EmoteInstance.instance.getClientMethods().getPerspective() == FPPerspective) {
            emotePlayer.perspective = 1;
            EmoteInstance.instance.getClientMethods().setPerspective(TPBPerspective.get());
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

    int emotes_getAge();
    int emotes_getAndIncreaseAge();

    @Override
    default void emoteTick(){
        if(emotes_getAge() <= 1){ //Emote init with a little delay (40-60 ms)
            if(this.emotes_getAndIncreaseAge() == 1) initEmotePlay();
        }

        if(isPlayingEmote()){
            setBodyYaw(getViewYaw());
            emoteTickCallback();
            if(this.isMainPlayer() && getEmote().perspective == 1 && EmoteInstance.instance.getClientMethods().getPerspective() != TPBPerspective.get()){
                this.getEmote().perspective = 0;
            }
            if(this.isMainPlayer() && !this.isForcedEmote() && !ClientPacketManager.isRemoteTracking() && !EmoteHolder.canRunEmote(this)){
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
    default void stopEmote(UUID emoteID){
        if(getEmote() != null && getEmote().getData().getUuid().equals(emoteID)){
            this.getEmote().stop();
            this.voidEmote();
        }
    }

    void voidEmote();
}
