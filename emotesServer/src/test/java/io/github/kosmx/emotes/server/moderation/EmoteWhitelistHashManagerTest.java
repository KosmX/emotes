package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.testing.common.RandomEmoteData;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import java.util.Collections;
import java.util.HashMap;
import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EmoteWhitelistHashManagerTest {
    private Animation makeAnimationWithSound(SoundKeyframeData sound) {
        return new Animation(
                new ExtraAnimationData(),
                20,
                Animation.LoopType.PLAY_ONCE,
                Collections.emptyMap(),
                new Animation.Keyframes(
                        sound == null ? new SoundKeyframeData[0] : new SoundKeyframeData[]{sound},
                        new ParticleKeyframeData[0],
                        new CustomInstructionKeyframeData[0]
                ),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    private Animation makeAnimationWithParticle(ParticleKeyframeData particle) {
        return new Animation(
                new ExtraAnimationData(),
                20,
                Animation.LoopType.PLAY_ONCE,
                Collections.emptyMap(),
                new Animation.Keyframes(
                        new SoundKeyframeData[0],
                        particle == null ? new ParticleKeyframeData[0] : new ParticleKeyframeData[]{particle},
                        new CustomInstructionKeyframeData[0]
                ),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    @Test
    @DisplayName("calculateEmoteHash returns consistent hash for same emote")
    public void testConsistentHash() {
        Pair<Animation, Animation> emotes = RandomEmoteData.generateEmotes();
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(emotes.left());
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(emotes.left());
        Assertions.assertEquals(hash1, hash2, "Hash should be consistent for the same emote");
    }

    @Test
    @DisplayName("calculateEmoteHash returns different hashes for different emotes")
    public void testDifferentHash() {
        Pair<Animation, Animation> emotes = RandomEmoteData.generateDifferentEmotes();
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(emotes.left());
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(emotes.right());
        Assertions.assertNotEquals(hash1, hash2, "Different emotes should have different hashes");
    }

    @Test
    @DisplayName("Different sounds produce different hashes")
    public void testDifferentSounds() {
        SoundKeyframeData sound1 = new SoundKeyframeData(0f, "soundA");
        SoundKeyframeData sound2 = new SoundKeyframeData(0f, "soundB");
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithSound(sound1));
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithSound(sound2));
        Assertions.assertNotEquals(hash1, hash2, "Different sounds should produce different hashes");
    }

    @Test
    @DisplayName("Different particles produce different hashes")
    public void testDifferentParticles() {
        ParticleKeyframeData particle1 = new ParticleKeyframeData(0f, "particleA", "idA", "paramA");
        ParticleKeyframeData particle2 = new ParticleKeyframeData(0f, "particleB", "idB", "paramB");
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithParticle(particle1));
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithParticle(particle2));
        Assertions.assertNotEquals(hash1, hash2, "Different particles should produce different hashes");
    }

    @Test
    @DisplayName("Adding a sound changes the hash")
    public void testAddingSoundChangesHash() {
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithSound(null));
        SoundKeyframeData sound = new SoundKeyframeData(0f, "soundA");
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithSound(sound));
        Assertions.assertNotEquals(hash1, hash2, "Adding a sound should change the hash");
    }

    @Test
    @DisplayName("Adding a particle changes the hash")
    public void testAddingParticleChangesHash() {
        
        int hash1 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithParticle(null));
        ParticleKeyframeData particle = new ParticleKeyframeData(0f, "particleA", "idA", "paramA");
        int hash2 = EmoteWhitelistManager.calculateEmoteHash(makeAnimationWithParticle(particle));
        Assertions.assertNotEquals(hash1, hash2, "Adding a particle should change the hash");
    }
}
