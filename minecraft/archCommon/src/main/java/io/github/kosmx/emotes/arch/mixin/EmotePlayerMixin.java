package io.github.kosmx.emotes.arch.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.emotePlay.InstrumentConventer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.raphimc.noteblocklib.model.Note;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

//Mixin it into the player is way easier than storing it somewhere else...
@Mixin(AbstractClientPlayer.class)
public abstract class EmotePlayerMixin extends Player implements IPlayerEntity {

    @Unique
    private int emotecraft$age = 0;

    @Unique
    private final EmotePlayer emotecraft$container = new EmotePlayer((AbstractClientPlayer) (Object) this);

    @Unique
    private boolean emotecraft$isForced = false;

    public EmotePlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        ((IAnimatedPlayer) this).playerAnimLib$getAnimManager().addAnimLayer(1000, emotecraft$container);
    }

    @Override
    public void emotecraft$playEmote(Animation emote, float tick, boolean isForced) {
        stopEmote();
        this.emotecraft$container.triggerAnimation(RawAnimation.begin().thenPlay(emote), tick);
        // this.initEmotePerspective(emotecraft$container.getAnim());
        if (this.isMainPlayer()) this.emotecraft$isForced = isForced;
    }

    @Unique
    private void emotecraft$noteConsumer(Note note) {
        emotecraft$playRawSound(InstrumentConventer.getInstrument(note, position()), true);
    }

    @Override
    public void emotecraft$playRawSound(SoundInstance instance, boolean distanceDelay) {
        double d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(position());
        if (distanceDelay && d > 100.0) {
            double e = Math.sqrt(d) / 40.0;
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().getSoundManager().playDelayed(instance, (int)(e * 20.0)));
        } else {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().getSoundManager().play(instance));
        }
    }

    @Nullable
    @Override
    public EmotePlayer emotecraft$getEmote() {
        return this.emotecraft$container;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.emotecraft$age <= 1) { //Emote init with a little delay (40-60 ms)
            if(this.emotecraft$age++ == 1) {
                Pair<Animation, Float> p = ClientEmotePlay.getEmoteForUUID(getUUID());
                if(p != null){
                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.left(), p.right(), getUUID());
                    this.emotecraft$playEmote(p.left(), p.right(), false);
                }
                if(!this.isMainPlayer() && PlatformTools.getMainPlayer() != null && PlatformTools.getMainPlayer().isPlayingEmote()){
                    IPlayerEntity playerEntity = PlatformTools.getMainPlayer();
                    ClientEmotePlay.clientRepeatLocalEmote(playerEntity.emotecraft$getEmote().getData(), playerEntity.emotecraft$getEmote().getAnimationTicks(), this.getUUID());
                }
            }
        }

        if (isPlayingEmote()) {
            this.yBodyRot = this.yHeadRot;

            EmotePlayer emotePlayer = emotecraft$getEmote();

            if (isMainPlayer() && emotePlayer != null) {
                /*if (emotePlayer.perspective == 1 && PlatformTools.getPerspective() != TPBPerspective.get()) {
                    emotePlayer.perspective = 0;
                }*/

                if(!this.emotecraft$isForcedEmote() && !EmoteHolder.canRunEmote((AbstractClientPlayer) (Object) this)) {
                    emotePlayer.stop();
                    ClientEmotePlay.clientStopLocalEmote(emotePlayer.getData());
                }
            }
        }
    }

    @Override
    public boolean emotecraft$isForcedEmote() {
        return this.isPlayingEmote() && this.emotecraft$isForced;
    }
}
