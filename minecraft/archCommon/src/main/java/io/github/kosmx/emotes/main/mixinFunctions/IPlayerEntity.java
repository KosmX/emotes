package io.github.kosmx.emotes.main.mixinFunctions;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;


public interface IPlayerEntity extends IEmotePlayerEntity {

    int FPPerspective = 0;
    Supplier<Integer> TPBPerspective = () -> (((ClientConfig)EmoteInstance.config).frontAsTPPerspective.get() ? 2 : 1);

    default void initEmotePlay(){

        Pair<KeyframeAnimation, Integer> p = ClientEmotePlay.getEmoteForUUID(this.emotes_getUUID());
        if(p != null){
            ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.getLeft(), this.emotes_getUUID());
            this.emotecraft$playEmote(p.getLeft(), p.getRight(), false);
        }
        if(!this.isMainPlayer() && TmpGetters.getClientMethods().getMainPlayer() != null && TmpGetters.getClientMethods().getMainPlayer().isPlayingEmote()){
            IPlayerEntity playerEntity = TmpGetters.getClientMethods().getMainPlayer();
            ClientEmotePlay.clientRepeatLocalEmote(playerEntity.emotecraft$getEmote().getData(), playerEntity.emotecraft$getEmote().getTick(), this.emotes_getUUID());
        }

    }

    default void initEmotePerspective(EmotePlayer emotePlayer){
        if(((ClientConfig)EmoteInstance.config).enablePerspective.get() && isMainPlayer() && TmpGetters.getClientMethods().getPerspective() == FPPerspective) {
            emotePlayer.perspective = 1;
            TmpGetters.getClientMethods().setPerspective(TPBPerspective.get());
        }
    }

    @Nullable
    EmotePlayer emotecraft$getEmote();

    @Override
    default boolean isPlayingEmote(){
        return EmotePlayer.isRunningEmote(this.emotecraft$getEmote());
    }

    @Override
    default boolean isMainPlayer(){
        return TmpGetters.getClientMethods().getMainPlayer() == this;
    }

    int emotes_getAge();
    int emotes_getAndIncreaseAge();

    @Override
    default void emoteTick(){
        if(emotes_getAge() <= 1){ //Emote init with a little delay (40-60 ms)
            if(this.emotes_getAndIncreaseAge() == 1) initEmotePlay();
        }

        if(isPlayingEmote()){
            emotecraft$setBodyYaw(emotecraft$getViewYaw());
            emoteTickCallback();
            if(this.isMainPlayer() && emotecraft$getEmote().perspective == 1 && TmpGetters.getClientMethods().getPerspective() != TPBPerspective.get()){
                this.emotecraft$getEmote().perspective = 0;
            }
            if(this.isMainPlayer() && !this.emotecraft$isForcedEmote() && !EmoteHolder.canRunEmote(this)){
                this.emotecraft$getEmote().stop();
                ClientEmotePlay.clientStopLocalEmote(this.emotecraft$getEmote().getData());
            }
        }
    }

    @Override
    default void stopEmote(){
        if(emotecraft$getEmote() != null) {
            this.emotecraft$getEmote().stop();
            this.emotecraft$voidEmote();
        }
    }

    @Override
    default void stopEmote(UUID emoteID){
        if(emotecraft$getEmote() != null && emotecraft$getEmote().getData().getUuid().equals(emoteID)){
            this.emotecraft$getEmote().stop();
            this.emotecraft$voidEmote();
        }
    }

    void emotecraft$voidEmote();
}
