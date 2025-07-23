package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.InputStream;
import java.util.zip.CRC32;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Files;
import io.github.kosmx.emotes.server.services.InstanceService;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
    
    /**
     * Setup whitelist config and moderation. Used by both MainLoader and BukkitWrapper.
     * @param doHash If true, hash emotes after setup. Default is true.
     */
    public static void setupWhitelistConfig() {
        setupWhitelistConfig(true);
    }

    public static void setupWhitelistConfig(boolean doHash) {
        if (Serializer.getConfig().enableEmoteWhitelist.get()) {
            Path whitelistDir = InstanceService.INSTANCE.getGameDirectory().resolve(Serializer.getConfig().whitelistedEmotesDir.get());
            createWhitelistDirIfNeeded(whitelistDir);
            EmoteWhitelistHashManager hashManager = getInstance();
            Path jarHashFile = whitelistDir.resolve("emotecraft_jar_hash.txt");
            try {
                String jarPath = EmoteWhitelistHashManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                if (System.getProperty("os.name").toLowerCase().contains("win") && jarPath.length() > 2 && jarPath.charAt(0) == '/' && jarPath.charAt(2) == ':') {
                    jarPath = jarPath.substring(1);
                }
                Path jarFile = InstanceService.INSTANCE.getGameDirectory().resolve(jarPath);
                String jarHash = computeFileCRC32(jarFile);
                boolean refresh = false;
                if (Files.exists(jarHashFile)) {
                    String storedHash = Files.readAllLines(jarHashFile).get(0).trim();
                    if (!jarHash.equals(storedHash)) {
                        refresh = true;
                    }
                } else {
                    refresh = true;
                }
                if (refresh) {
                    hashManager.forceRefreshHashes();
                    Files.writeString(jarHashFile, jarHash + "\n");
                }
            } catch (Exception e) {
                CommonData.LOGGER.warn("Failed to check/store jar hash on startup, this is not a critical error, but consider force reloading your whitelisted emotes when you update emotecraft (it will not be done automatically)", e);
            }
            if (doHash) {
                hashManager.hashEmotes();
            }
        }
    }

    private static String computeFileCRC32(Path file) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                crc.update(buffer, 0, len);
            }
        }
        return Long.toHexString(crc.getValue());
    }


    private static EmoteWhitelistHashManager INSTANCE;

    private static final String HASHES_FILE = "emoteHashes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, EmoteFileInfo> fileInfoMap = new HashMap<>();
    private final Set<Integer> allowedHashes = new HashSet<>();
    private final Path whitelistDir;
    private final Path hashesFile;

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
            INSTANCE = new EmoteWhitelistHashManager(InstanceService.INSTANCE.getGameDirectory().resolve(Serializer.getConfig().whitelistedEmotesDir.get()));
        }
        return INSTANCE;
    }

    public EmoteWhitelistHashManager(Path whitelistDir) {
        this.whitelistDir = whitelistDir;
        this.hashesFile = whitelistDir.resolve(HASHES_FILE);
    }

    public boolean isHashAllowed(int hash) {
        return allowedHashes.contains(hash);
    }

    /**
     * Force refresh: delete emoteHashes.json and rebuild from whitelist folder
     */
    public void forceRefreshHashes() {
        CommonData.LOGGER.warn("Refreshing all emotes for the whitelist [first run or updated Emotecraft], this may take a while if you have many whitelisted emotes.");
        try {
            if (Files.exists(hashesFile)) {
                Files.delete(hashesFile);
            }
        } catch (Exception e) {
            CommonData.LOGGER.warn("Failed to delete emoteHashes.json for force refresh", e);
        }
    }

    public void hashEmotes() {
        Set<String> foundFiles = new HashSet<>();

        // Load previous hashes from emoteHashes.json if exists
        final Map<String, EmoteFileInfo> previousHashes = new HashMap<>();
        if (Files.exists(hashesFile)) {
            try (Reader reader = Files.newBufferedReader(hashesFile)) {
                Type type = new TypeToken<Map<String, EmoteFileInfo>>(){}.getType();
                Map<String, EmoteFileInfo> loaded = GSON.fromJson(reader, type);
                if (loaded != null) previousHashes.putAll(loaded);
            } catch (Exception e) {
                CommonData.LOGGER.warn("Failed to read previous emote hashes", e);
            }
        }

        fileInfoMap.clear();
        try {
            Files.walkFileTree(whitelistDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }

                    boolean isJson = fileName.endsWith(".json") && !fileName.equals(HASHES_FILE);
                    boolean isEmotecraft = fileName.endsWith(".emotecraft");
                    if (!isJson && !isEmotecraft) {
                        return FileVisitResult.CONTINUE;
                    }

                    String relFileName = whitelistDir.relativize(file).toString().replace('\\', '/');
                    foundFiles.add(relFileName);
                    long lastMod = attrs.lastModifiedTime().toMillis();
                    EmoteFileInfo prevInfo = previousHashes.get(relFileName);
                    boolean needsUpdate = prevInfo == null || lastMod > prevInfo.lastModified;
                    if (needsUpdate) {
                        try (InputStream reader = Files.newInputStream(file)) {
                            List<Animation> emotes = UniversalEmoteSerializer.readData(reader, fileName);
                            for (Animation emote : emotes) {
                                int hash = calculateEmoteHash(emote);
                                fileInfoMap.put(relFileName, new EmoteFileInfo(relFileName, lastMod, hash));
                                CommonData.LOGGER.info("Hashed emote file {} (hash {})", relFileName, hash);
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

        for (String f : previousHashes.keySet()) {
            if (!foundFiles.contains(f)) {
                CommonData.LOGGER.info("Removed emote from whitelist (no longer present in directory): {}", f);
            }
        }

        save();
        CommonData.LOGGER.info("{} emotes whitelisted: ", fileInfoMap.size());
        updateAllowedHashes();
    }

    /**
     * Update allowedHashes from fileInfoMap
     */
    private void updateAllowedHashes() {
        allowedHashes.clear();
        for (EmoteFileInfo info : fileInfoMap.values()) {
            allowedHashes.add(info.hash);
        }
    }

    private void save() {
        try (Writer writer = Files.newBufferedWriter(hashesFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(fileInfoMap, writer);
        } catch (IOException e) {
            CommonData.LOGGER.warn("Failed to write emoteHashes.json", e);
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
