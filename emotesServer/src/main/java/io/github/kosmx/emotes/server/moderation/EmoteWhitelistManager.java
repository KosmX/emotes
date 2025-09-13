package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EmoteWhitelistManager {
    private static final Set<Integer> allowedHashes = new HashSet<>();

    public static void setupWhitelistConfig(boolean doHash) {
        if (Serializer.getConfig().enableEmoteWhitelist.get()) {
            Path whitelistDir = InstanceService.INSTANCE.getConfigFolder().resolve(Serializer.getConfig().whitelistedEmotesDir.get());
            try {
                if (!Files.exists(whitelistDir)) {
                    Files.createDirectories(whitelistDir);
                    CommonData.LOGGER.info("Created whitelist emotes directory: {}", whitelistDir.toAbsolutePath());
                }
            } catch (IOException e) {
                CommonData.LOGGER.warn("Failed to create whitelist emotes directory: {}", whitelistDir.toAbsolutePath(), e);
            }
            if (doHash) {
                allowedHashes.clear();
                UUIDMap<Animation> animations = new UUIDMap<>();
                EmoteSerializer.serializeEmotes(animations, InstanceService.INSTANCE.getConfigFolder().resolve(Serializer.getConfig().whitelistedEmotesDir.get()));
                addEmotesToAllowedHashes(animations.values());
            }
        }
    }

    public static boolean isHashAllowed(int hash) {
        return allowedHashes.contains(hash);
    }

    public static void addEmotesToAllowedHashes(Collection<Animation> animations) {
        for (Animation animation : animations) {
            allowedHashes.add(calculateEmoteHash(animation));
        }
    }

    public static int calculateEmoteHash(Animation emote) {
        int hash = emote.boneAnimations().hashCode();
        for (SoundKeyframeData sound : emote.keyFrames().sounds()) {
            hash = combineHash(hash, sound.hashCode());
        }
        for (ParticleKeyframeData particle : emote.keyFrames().particles()) {
            hash = combineHash(hash, particle.hashCode());
        }
        return hash;
    }

    private static int combineHash(int existing, int newValue) {
        return 31 * existing + newValue;
    }

    static {
        ServerEmoteEvents.EMOTE_VERIFICATION.register(EmoteModerator::verifyEmote);
    }
}
