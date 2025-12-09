package io.github.kosmx.emotes.main.emotePlay.instances;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundDirectInstance implements SoundInstance {
    private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

    protected final Sound sound;
    protected final float volume;
    protected final float pitch;
    protected final Vec3 pos;

    public SoundDirectInstance(Identifier sound, float volume, float pitch, Vec3 pos) {
        this(new Sound(sound, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16), volume, pitch, pos);
    }

    public SoundDirectInstance(Sound sound, float volume, float pitch, Vec3 pos) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.pos = pos;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return this.sound.getLocation();
    }

    @Override
    public @Nullable WeighedSoundEvents resolve(SoundManager manager) {
        return new EmotecraftSoundEvents(this.sound);
    }

    @Override
    public @NotNull Sound getSound() {
        return this.sound;
    }

    @Override
    public @NotNull SoundSource getSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public double getX() {
        return this.pos.x() + 0.5;
    }

    @Override
    public double getY() {
        return this.pos.y() + 0.5;
    }

    @Override
    public double getZ() {
        return this.pos.z() + 0.5;
    }

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
