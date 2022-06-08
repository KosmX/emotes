package io.github.kosmx.emotes.arch.mixin;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.arch.emote.AnimationApplier;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.opennbs.format.Layer;
import io.github.kosmx.emotes.common.tools.Vec3d;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.playerAnim.IAnimatedPlayer;
import io.github.kosmx.playerAnim.impl.AnimationPlayer;
import io.github.kosmx.playerAnim.layered.AnimationContainer;
import io.github.kosmx.playerAnim.layered.AnimationStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

//Mixin it into the player is way easier than storing it somewhere else...
@Mixin(AbstractClientPlayer.class)
public abstract class EmotePlayerMixin extends Player implements IPlayerEntity<ModelPart>, IAnimatedPlayer {
    int emotes_age = 0;

    @Shadow @Final public ClientLevel clientLevel;

    private final AnimationStack animationStack = new AnimationStack();
    private final AnimationApplier animationApplier = new AnimationApplier(animationStack);

    AnimationContainer<EmotePlayer<ModelPart>> emotecraftEmoteContainer = new AnimationContainer<>(null);

    boolean isForced = false;

    public EmotePlayerMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void emotecraft_init(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        animationStack.addAnimLayer(0, emotecraftEmoteContainer);
    }

    @Override
    public void playEmote(EmoteData emote, int t, boolean isForced) {
        this.emotecraftEmoteContainer.setAnim(new EmotePlayImpl(emote, this::noteConsumer, t));
        this.initEmotePerspective(emotecraftEmoteContainer.getAnim());
        if (this.isMainPlayer()) this.isForced = isForced;
    }

    private void noteConsumer(Layer.Note note){
        this.clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), getInstrumentFromCode(note.instrument).getSoundEvent(), SoundSource.PLAYERS, note.getVolume(), note.getPitch(), true);
    }

    private static NoteBlockInstrument getInstrumentFromCode(byte b){

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
    public int emotes_getAge() {
        return this.emotes_age;
    }

    @Override
    public int emotes_getAndIncreaseAge() {
        return this.emotes_age++;
    }

    @Override
    public void voidEmote() {
        this.emotecraftEmoteContainer.setAnim(null);
    }


    @Nullable
    @Override
    public EmotePlayer<ModelPart> getEmote() {
        return this.emotecraftEmoteContainer.getAnim();
    }

    @Override
    public UUID emotes_getUUID() {
        return this.getUUID();
    }

    @Override
    public boolean isNotStanding() {
        return this.getPose() != Pose.STANDING;
    }

    @Override
    public Vec3d emotesGetPos() {
        return new Vec3d(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public Vec3d getPrevPos() {
        return new Vec3d(xo, yo, zo);
    }

    @Override
    public float getBodyYaw() {
        return this.yBodyRot;
    }

    @Override
    public float getViewYaw() {
        return this.yHeadRot;
    }

    @Override
    public void setBodyYaw(float newYaw) {
        this.yBodyRot = newYaw;
    }

    @Override
    public void tick() {
        super.tick();
        this.animationStack.tick();
        this.emoteTick();
    }

    @Override
    public boolean isForcedEmote() {
        return this.isPlayingEmote() && this.isForced;
    }

    @Override
    public AnimationPlayer getAnimation() {
        return this.animationApplier;
    }

    @Override
    public AnimationStack getAnimationStack() {
        return animationStack;
    }
}
