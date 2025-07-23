package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import io.github.kosmx.emotes.server.services.InstanceService;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.config.Serializer;

/**
 * Manages emote hashes for whitelist folder.
 */
public class EmoteWhitelistHashManager {
    /**
     * Force-reload the emote whitelist: deletes hashes file, then re-hashes all emotes.
     */
    public static void forceReloadWhitelist() {
        EmoteWhitelistHashManager manager = getInstance();
        manager.forceRefreshHashes();
        manager.hashEmotes();
    }

    public static void setupWhitelistConfig(boolean doHash) {
        if (Serializer.getConfig().enableEmoteWhitelist.get()) {
            Path whitelistDir = InstanceService.INSTANCE.getConfigFolder().resolve(Serializer.getConfig().whitelistedEmotesDir.get());
            createWhitelistDirIfNeeded(whitelistDir);
            EmoteWhitelistHashManager hashManager = getInstance();
            if (doHash) {
                hashManager.hashEmotes();
            }
        }
    }

    private static EmoteWhitelistHashManager INSTANCE;

    private final Map<String, EmoteFileInfo> fileInfoMap = new HashMap<>();
    private final Set<Integer> allowedHashes = new HashSet<>();
    private final Path whitelistDir;

    /**
     * Ensure the whitelist directory exists. If created for the first time, log and perform any custom setup.
     */
    public static void createWhitelistDirIfNeeded(Path whitelistDir) {
        try {
            if (!Files.exists(whitelistDir)) {
                Files.createDirectories(whitelistDir);
                CommonData.LOGGER.info("Created whitelist emotes directory: {}", whitelistDir.toAbsolutePath());
                // Add all internal emotes to the whitelist directory
                List<String> copiedEmotes = new ArrayList<>();
                try {
                    String[] internalEmotes = {
                        "waving", "clap", "crying", "point", "here", "palm", "backflip",
                        "roblox_potion_dance", "kazotsky_kick", "twerk", "club_penguin_dance"
                    };
                    for (String emoteName : internalEmotes) {
                        String jsonPath = "/assets/" + CommonData.MOD_ID + "/emotes/" + emoteName + ".json";
                        try (InputStream emoteStream = EmoteWhitelistHashManager.class.getResourceAsStream(jsonPath)) {
                            if (emoteStream != null) {
                                Path targetFile = whitelistDir.resolve(emoteName + ".json");
                                Files.copy(emoteStream, targetFile);
                                copiedEmotes.add(emoteName);
                            } else {
                                CommonData.LOGGER.warn("Internal emote resource not found: {}", jsonPath);
                            }
                        }
                    }
                    if (!copiedEmotes.isEmpty()) {
                        CommonData.LOGGER.info("Added internal emotes to whitelist directory: {}", String.join(", ", copiedEmotes));
                    }
                } catch (Exception e) {
                    CommonData.LOGGER.warn("Failed to copy internal emotes to whitelist directory", e);
                }
            }
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to create whitelist emotes directory: {}", whitelistDir.toAbsolutePath(), e);
        }
    }

    public static class EmoteFileInfo {
        public String fileName;
        public long lastModified;
        public int hash;
        public EmoteFileInfo(String fileName, long lastModified, int hash) {
            this.fileName = fileName;
            this.lastModified = lastModified;
            this.hash = hash;
        }
    }

    public static EmoteWhitelistHashManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EmoteWhitelistHashManager(InstanceService.INSTANCE.getConfigFolder().resolve(Serializer.getConfig().whitelistedEmotesDir.get()));
        }
        return INSTANCE;
    }

    public EmoteWhitelistHashManager(Path whitelistDir) {
        this.whitelistDir = whitelistDir;
        EmoteModerator.register();
    }

    public boolean isHashAllowed(int hash) {
        return allowedHashes.contains(hash);
    }

    public void forceRefreshHashes() {
        fileInfoMap.clear();
    }

    public void hashEmotes() {
        Set<String> foundFiles = new HashSet<>();
        boolean wasEmpty = fileInfoMap.isEmpty();
        Map<String, EmoteFileInfo> previousHashes = new HashMap<>(fileInfoMap);
        fileInfoMap.clear();
        try {
            Files.walkFileTree(whitelistDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    boolean isJson = fileName.endsWith(".json");
                    boolean isEmotecraft = fileName.endsWith(".emotecraft");
                    if (!isJson && !isEmotecraft) {
                        return FileVisitResult.CONTINUE;
                    }

                    String relFileName = whitelistDir.relativize(file).toString();
                    foundFiles.add(relFileName);
                    long lastMod = attrs.lastModifiedTime().toMillis();
                    EmoteFileInfo prevInfo = previousHashes.get(relFileName);                    
                    if (prevInfo == null || lastMod > prevInfo.lastModified) {
                        try (InputStream reader = Files.newInputStream(file)) {
                            List<Animation> emotes = UniversalEmoteSerializer.readData(reader, fileName);
                            for (Animation emote : emotes) {
                                int hash = calculateEmoteHash(emote);
                                fileInfoMap.put(relFileName, new EmoteFileInfo(relFileName, lastMod, hash));
                                if (!wasEmpty) {
                                    CommonData.LOGGER.info("Hashed emote file {} (hash {})", relFileName, hash);
                                }
                            }
                        } catch (Throwable th) {
                            CommonData.LOGGER.warn("Error while importing emote for hashing: {}", file.getFileName(), th);
                        }
                    } else {
                        fileInfoMap.put(relFileName, prevInfo);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to walk whitelist directory", e);
        }

        if (!wasEmpty) {
            for (String f : previousHashes.keySet()) {
                if (!foundFiles.contains(f)) {
                    CommonData.LOGGER.info("Removed emote from whitelist (no longer present in directory): {}", f);
                }
            }
        }

        CommonData.LOGGER.info("{} emotes whitelisted: ", fileInfoMap.size());
        
        allowedHashes.clear();
        for (EmoteFileInfo info : fileInfoMap.values()) {
            allowedHashes.add(info.hash);
        }
    }


    public int calculateEmoteHash(Animation emote) {
        int hash = emote.boneAnimations().hashCode();
        for (SoundKeyframeData sound : emote.keyFrames().sounds()) {
            hash = combineHash(hash, sound.hashCode());
        }
        for (ParticleKeyframeData particle : emote.keyFrames().particles()) {
            hash = combineHash(hash, particle.hashCode());
        }
        return hash;
    }

    private int combineHash(int existing, int newValue) {
        return 31 * existing + newValue;
    }
}
