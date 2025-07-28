package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.util.*;

public class EmoteWhitelistHashManagerSoundParticleTest {
    private Animation makeAnimationWithSound(SoundKeyframeData sound) {
        return new Animation(
                new ExtraAnimationData(),
                20,
                Animation.LoopType.PLAY_ONCE,
                Collections.emptyMap(),
                UniversalAnimLoader.NO_KEYFRAMES,
                sound == null ? new HashMap<>() : Map.of("0", List.of(sound)),
                new HashMap<>()
        );
    }

    private Animation makeAnimationWithParticle(ParticleKeyframeData particle) {
        return new Animation(
                new ExtraAnimationData(),
                20,
                Animation.LoopType.PLAY_ONCE,
                Collections.emptyMap(),
                UniversalAnimLoader.NO_KEYFRAMES,
                new HashMap<>(),
                particle == null ? new HashMap<>() : Map.of("0", List.of(particle))
        );
    }

    @Test
    @DisplayName("Different sounds produce different hashes")
    public void testDifferentSounds() {
        SoundKeyframeData sound1 = new SoundKeyframeData(0f, "soundA");
        SoundKeyframeData sound2 = new SoundKeyframeData(0f, "soundB");
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(makeAnimationWithSound(sound1));
        int hash2 = manager.calculateEmoteHash(makeAnimationWithSound(sound2));
        Assertions.assertNotEquals(hash1, hash2, "Different sounds should produce different hashes");
    }

    @Test
    @DisplayName("Different particles produce different hashes")
    public void testDifferentParticles() {
        ParticleKeyframeData particle1 = new ParticleKeyframeData(0f, "particleA", "idA", "paramA");
        ParticleKeyframeData particle2 = new ParticleKeyframeData(0f, "particleB", "idB", "paramB");
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(makeAnimationWithParticle(particle1));
        int hash2 = manager.calculateEmoteHash(makeAnimationWithParticle(particle2));
        Assertions.assertNotEquals(hash1, hash2, "Different particles should produce different hashes");
    }

    @Test
    @DisplayName("Adding a sound changes the hash")
    public void testAddingSoundChangesHash() {
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(makeAnimationWithSound(null));
        SoundKeyframeData sound = new SoundKeyframeData(0f, "soundA");
        int hash2 = manager.calculateEmoteHash(makeAnimationWithSound(sound));
        Assertions.assertNotEquals(hash1, hash2, "Adding a sound should change the hash");
    }

    @Test
    @DisplayName("Adding a particle changes the hash")
    public void testAddingParticleChangesHash() {
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(makeAnimationWithParticle(null));
        ParticleKeyframeData particle = new ParticleKeyframeData(0f, "particleA", "idA", "paramA");
        int hash2 = manager.calculateEmoteHash(makeAnimationWithParticle(particle));
        Assertions.assertNotEquals(hash1, hash2, "Adding a particle should change the hash");
    }
}
