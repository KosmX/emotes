package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.testing.common.RandomEmoteData;
import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EmoteWhitelistHashManagerTest {
    @Test
    @DisplayName("calculateEmoteHash returns consistent hash for same emote")
    public void testConsistentHash() {
        Pair<Animation, Animation> emotes = RandomEmoteData.generateEmotes();
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(emotes.left());
        int hash2 = manager.calculateEmoteHash(emotes.left());
        Assertions.assertEquals(hash1, hash2, "Hash should be consistent for the same emote");
    }

    @Test
    @DisplayName("calculateEmoteHash returns different hashes for different emotes")
    public void testDifferentHash() {
        Pair<Animation, Animation> emotes = RandomEmoteData.generateDifferentEmotes();
        EmoteWhitelistHashManager manager = new EmoteWhitelistHashManager(null);
        int hash1 = manager.calculateEmoteHash(emotes.left());
        int hash2 = manager.calculateEmoteHash(emotes.right());
        Assertions.assertNotEquals(hash1, hash2, "Different emotes should have different hashes");
    }
}
