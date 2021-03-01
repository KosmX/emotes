package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.opennbs.format.Layer;
import com.kosmx.emotes.common.tools.Vec3d;
import com.kosmx.emotes.fabric.emote.EmotePlayImpl;
import com.kosmx.emotes.main.emotePlay.EmotePlayer;
import com.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.UUID;

//Mixin it into the player is way easier than storing it somewhere else...
@Mixin(AbstractClientPlayerEntity.class)
public abstract class EmotePlayerMixin extends PlayerEntity implements IPlayerEntity<ModelPart> {
    @Shadow @Final public ClientWorld clientWorld;
    @Nullable EmotePlayer<ModelPart> emote;

    public EmotePlayerMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void playEmote(EmoteData emote) {
        this.emote = new EmotePlayImpl(emote, this::noteConsumer);
    }

    private void noteConsumer(Layer.Note note){
        this.clientWorld.playSound(this.getX(), this.getY(), this.getZ(), getInstrumentFromCode(note.instrument).getSound(), SoundCategory.PLAYERS, note.getVolume(), note.getPitch(), true);
    }

    private static Instrument getInstrumentFromCode(byte b){

        //That is more efficient than a switch case...
        Instrument[] instruments = {Instrument.HARP, Instrument.BASS, Instrument.BASEDRUM, Instrument.SNARE, Instrument.HAT,
                Instrument.GUITAR, Instrument.FLUTE, Instrument.BELL, Instrument.CHIME, Instrument.XYLOPHONE,Instrument.IRON_XYLOPHONE,
                Instrument.COW_BELL, Instrument.DIDGERIDOO, Instrument.BIT, Instrument.BANJO, Instrument.PLING};

        if(b >= 0 && b < instruments.length){
            return instruments[b];
        }
        return Instrument.HARP; //I don't want to crash here
    }

    @Override
    public void voidEmote() {
        this.emote = null;
    }

    @Nullable
    @Override
    public EmotePlayer<ModelPart> getEmote() {
        return this.emote;
    }

    @Override
    public UUID getUUID() {
        return this.getUuid();
    }

    @Override
    public boolean isNotStanding() {
        return this.getPose() != EntityPose.STANDING;
    }

    @Override
    public Vec3d emotesGetPos() {
        return new Vec3d(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public Vec3d getPrevPos() {
        return new Vec3d(prevX, prevY, prevZ);
    }

    @Override
    public float getBodyYaw() {
        return this.bodyYaw;
    }

    @Override
    public float getViewYaw() {
        return this.yaw;
    }

    @Override
    public void setBodyYaw(float newYaw) {
        this.bodyYaw = newYaw;
    }
}
