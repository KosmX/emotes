package io.github.kosmx.emotes.main.emotePlay.instances;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoundEventInstance extends AbstractSoundInstance {
    @SuppressWarnings("deprecation")
    private static final RandomSource RANDOM_SOURCE = RandomSource.createThreadSafe();

    public SoundEventInstance(SoundEvent soundEvent, float volume, float pitch, Vec3 pos) {
        super(soundEvent, SoundSource.PLAYERS, RANDOM_SOURCE);
        this.volume = volume;
        this.pitch = pitch;
        this.x = pos.x();
        this.y = pos.y();
        this.z = pos.z();
    }

    @Override
    public @NotNull WeighedSoundEvents resolve(SoundManager manager) {
        super.resolve(manager);
        return new EmotecraftSoundEvents(this.sound);
    }
}
