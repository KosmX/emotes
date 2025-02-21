package io.github.kosmx.emotes.arch.mixin;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.IPlayer;
import dev.kosmx.playerAnim.api.layered.AnimationContainer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.format.Layer;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow
    @Final
    public ClientLevel clientLevel;

    @Unique
    private final AnimationContainer<EmotePlayer> emotecraft$container = new AnimationContainer<>(null);

    @Unique
    private boolean emotecraft$isForced = false;

    public EmotePlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        ((IPlayer)this).playerAnimator$getAnimationStack().addAnimLayer(1000, emotecraft$container);
    }

    @Override
    public void emotecraft$playEmote(KeyframeAnimation emote, int t, boolean isForced) {
        this.emotecraft$container.setAnim(new EmotePlayer(emote, this::emotecraft$noteConsumer, t));
        this.initEmotePerspective(emotecraft$container.getAnim());
        if (this.isMainPlayer()) this.emotecraft$isForced = isForced;
    }

    @Unique
    private void emotecraft$noteConsumer(Layer.Note note){
        this.clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), emotecraft$getInstrumentFromCode(note.instrument).getSoundEvent().value(), SoundSource.PLAYERS, note.getVolume(), note.getPitch(), true);
    }

    @Unique
    private static NoteBlockInstrument emotecraft$getInstrumentFromCode(byte b){

        //That is more efficient than a switch case...
        NoteBlockInstrument[] instruments = {NoteBlockInstrument.HARP, NoteBlockInstrument.BASS, NoteBlockInstrument.BASEDRUM, NoteBlockInstrument.SNARE, NoteBlockInstrument.HAT,
                NoteBlockInstrument.GUITAR, NoteBlockInstrument.FLUTE, NoteBlockInstrument.BELL, NoteBlockInstrument.CHIME, NoteBlockInstrument.XYLOPHONE,NoteBlockInstrument.IRON_XYLOPHONE,
                NoteBlockInstrument.COW_BELL, NoteBlockInstrument.DIDGERIDOO, NoteBlockInstrument.BIT, NoteBlockInstrument.BANJO, NoteBlockInstrument.PLING};

        if(b >= 0 && b < instruments.length){
            return instruments[b];
        }
        return NoteBlockInstrument.HARP; //I don't want to crash here
    }

    @Override
    public void emotecraft$voidEmote() {
        this.emotecraft$container.setAnim(null);
    }

    @Nullable
    @Override
    public EmotePlayer emotecraft$getEmote() {
        return this.emotecraft$container.getAnim();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.emotecraft$age <= 1) { //Emote init with a little delay (40-60 ms)
            if(this.emotecraft$age++ == 1) {
                Pair<KeyframeAnimation, Integer> p = ClientEmotePlay.getEmoteForUUID(getUUID());
                if(p != null){
                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.getLeft(), p.getRight(), getUUID());
                    this.emotecraft$playEmote(p.getLeft(), p.getRight(), false);
                }
                if(!this.isMainPlayer() && PlatformTools.getMainPlayer() != null && PlatformTools.getMainPlayer().isPlayingEmote()){
                    IPlayerEntity playerEntity = PlatformTools.getMainPlayer();
                    ClientEmotePlay.clientRepeatLocalEmote(playerEntity.emotecraft$getEmote().getData(), playerEntity.emotecraft$getEmote().getTick(), this.getUUID());
                }
            }
        }

        if (isPlayingEmote()) {
            this.yBodyRot = this.yHeadRot;

            EmotePlayer emotePlayer = emotecraft$getEmote();

            if (isMainPlayer() && emotePlayer != null) {
                if (emotePlayer.perspective == 1 && PlatformTools.getPerspective() != TPBPerspective.get()) {
                    emotePlayer.perspective = 0;
                }

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
