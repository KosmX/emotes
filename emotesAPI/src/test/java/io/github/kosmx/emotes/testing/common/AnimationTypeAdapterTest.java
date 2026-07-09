package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.serializer.gson.AnimationTypeAdapter;
import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AnimationTypeAdapterTest {
    @Test
    @DisplayName("Animation Base64 round-trip")
    public void roundTrip() {
        Pair<Animation, Animation> pair = RandomEmoteData.generateEmotes();

        Animation restored = AnimationTypeAdapter.fromBase64(AnimationTypeAdapter.toBase64(pair.left()));

        Assertions.assertNotNull(restored, "Round-tripped animation should not be null");
        Assertions.assertEquals(pair.left().boneAnimations(), restored.boneAnimations(), "Round-trip must preserve the emote");
        Assertions.assertEquals(pair.left().boneAnimations().hashCode(), restored.boneAnimations().hashCode());
    }
}
