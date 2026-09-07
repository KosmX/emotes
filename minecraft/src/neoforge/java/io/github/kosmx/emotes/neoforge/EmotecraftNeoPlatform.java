package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.EmotecraftModPlatform;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.world.entity.Avatar;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;

public final class EmotecraftNeoPlatform implements EmotecraftModPlatform {
    @Override
    public String getModVersion(String modid) {
        ModFileInfo info = FMLLoader.getCurrent().getLoadingModList().getModFileById(modid);
        if (info == null) return modid.toUpperCase() + "-UNKNOWN-NEOFORGE";
        return info.versionString();
    }

    @Override
    public String getPlatformName() {
        return "neoforge";
    }

    @Override
    public EmoteSoundInstance createSound(Avatar avatar, OpusSound sound, IntSupplier offset) {
        return new EmoteSound(avatar, sound, offset);
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.neoforged.fml.loading.FMLLoader");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // NeoForge asks the same, through a method it patches into SoundInstance
    private static final class EmoteSound extends EmoteSoundInstance {
        private EmoteSound(Avatar avatar, OpusSound sound, IntSupplier offset) {
            super(avatar, sound, offset);
        }

        @Override
        public @NonNull CompletableFuture<AudioStream> getStream(@NonNull SoundBufferLibrary library, @NonNull Sound sound, boolean repeatInstantly) {
            return stream();
        }
    }
}
