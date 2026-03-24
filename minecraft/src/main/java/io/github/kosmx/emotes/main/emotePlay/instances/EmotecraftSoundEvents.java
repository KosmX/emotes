package io.github.kosmx.emotes.main.emotePlay.instances;

import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class EmotecraftSoundEvents extends WeighedSoundEvents {
    protected final Sound sound;

    public EmotecraftSoundEvents(Sound sound) {
        super(sound.getLocation(), CommonData.MOD_NAME);
        this.sound = sound;
    }

    @Override
    public @NotNull Sound getSound(RandomSource randomSource) {
        return this.sound;
    }

    @Override
    public int getWeight() {
        return this.sound.getWeight();
    }

    @Override
    public void addSound(Weighted<Sound> accessor) {
        // no-op
    }

    @Override
    public void preloadIfRequired(SoundEngine engine) {
        // no-op
    }
}
