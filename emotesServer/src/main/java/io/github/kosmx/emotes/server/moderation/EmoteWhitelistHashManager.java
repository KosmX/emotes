package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        io.github.kosmx.emotes.server.moderation.EmoteModerator.initialize();
        if (Serializer.getConfig().enableEmoteWhitelist.get()) {
            Path whitelistDir = Path.of(Serializer.getConfig().whitelistedEmotesDir.get());
            createWhitelistDirIfNeeded(whitelistDir);
            EmoteWhitelistHashManager hashManager = getInstance();
            Path jarLastModifiedFile = whitelistDir.resolve("emotecraft_last_modified.txt");
            try {
                String jarPath = EmoteWhitelistHashManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

                if (System.getProperty("os.name").toLowerCase().contains("win") && jarPath.length() > 2 && jarPath.charAt(0) == '/' && jarPath.charAt(2) == ':') {
                    jarPath = jarPath.substring(1);
                }

                Path jarFile = Path.of(jarPath);
                long jarLastModified = Files.getLastModifiedTime(jarFile).toMillis();
                if (Files.exists(jarLastModifiedFile)) {
                    String stored = Files.readAllLines(jarLastModifiedFile).get(0).trim();
                    long storedLastModified = 0;
                    try {
                        storedLastModified = Long.parseLong(stored);
                    } catch (NumberFormatException ignored) {}
                    if (jarLastModified > storedLastModified) {
                        hashManager.forceRefreshHashes();
                        String humanReadable = java.time.Instant.ofEpochMilli(jarLastModified)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        String fileContent = jarLastModified + "\n" + humanReadable + "\n";
                        Files.writeString(jarLastModifiedFile, fileContent);
                    }
                } else {
                    String humanReadable = java.time.Instant.ofEpochMilli(jarLastModified)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String fileContent = jarLastModified + "\n" + humanReadable + "\n";
                    Files.writeString(jarLastModifiedFile, fileContent);
                }

            } catch (Exception e) {
                CommonData.LOGGER.warn("Failed to check/store jar last modified time on startup", e);
            }
            if (doHash) {
                hashManager.hashEmotes();
            }
        }
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
                try {
                    // List of built-in emote names (see UniversalEmoteSerializer)
                    String[] internalEmotes = {
                        "waving", "clap", "crying", "point", "here", "palm", "backflip",
                        "roblox_potion_dance", "kazotsky_kick", "twerk", "club_penguin_dance"
                    };
                    for (String emoteName : internalEmotes) {
                        // Load the resource as a stream
                        String jsonPath = "/assets/" + CommonData.MOD_ID + "/emotes/" + emoteName + ".json";
                        try (InputStream emoteStream = EmoteWhitelistHashManager.class.getResourceAsStream(jsonPath)) {
                            if (emoteStream != null) {
                                Path targetFile = whitelistDir.resolve(emoteName + ".json");
                                Files.copy(emoteStream, targetFile);
                                CommonData.LOGGER.info("Copied internal emote '{}' to whitelist directory", emoteName);
                            } else {
                                CommonData.LOGGER.warn("Internal emote resource not found: {}", jsonPath);
                            }
                        }
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
            INSTANCE = new EmoteWhitelistHashManager(Path.of(Serializer.getConfig().whitelistedEmotesDir.get()));
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
        @SuppressWarnings("unchecked")
        final Map<String, EmoteFileInfo>[] previousHashesArr = (Map<String, EmoteFileInfo>[]) new Map[]{new HashMap<>()};
        if (Files.exists(hashesFile)) {
            try (Reader reader = Files.newBufferedReader(hashesFile)) {
                Type type = new com.google.gson.reflect.TypeToken<Map<String, EmoteFileInfo>>(){}.getType();
                previousHashesArr[0] = GSON.fromJson(reader, type);
                if (previousHashesArr[0] == null) previousHashesArr[0] = new HashMap<>();
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

                    // Process .json and .emotecraft files
                    boolean isJson = fileName.endsWith(".json") && !fileName.equals(HASHES_FILE);
                    boolean isEmotecraft = fileName.endsWith(".emotecraft");
                    if (!isJson && !isEmotecraft) {
                        return FileVisitResult.CONTINUE;
                    }

                    String relFileName = whitelistDir.relativize(file).toString().replace('\\', '/');
                    foundFiles.add(relFileName);
                    long lastMod = attrs.lastModifiedTime().toMillis();
                    EmoteFileInfo prevInfo = previousHashesArr[0].get(relFileName);
                    boolean needsUpdate = prevInfo == null || lastMod > prevInfo.lastModified;
                    if (needsUpdate) {
                        try (InputStream reader = Files.newInputStream(file)) {
                            List<Animation> emotes = UniversalEmoteSerializer.readData(reader, fileName);
                            for (Animation emote : emotes) {
                                int hash = calculateEmoteHash(emote);
                                fileInfoMap.put(relFileName, new EmoteFileInfo(relFileName, lastMod, hash));
                                CommonData.LOGGER.info("Hashed emote file: {}", relFileName);
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

        for (String f : previousHashesArr[0].keySet()) {
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
        for (var sound : emote.keyFrames().sounds()) {
            hash = combineHash(hash, sound.hashCode());
        }
        for (var particle : emote.keyFrames().particles()) {
            hash = combineHash(hash, particle.hashCode());
        }
        hash = combineHash(hash, emote.loopType().getClass().hashCode());
        return hash;
    }

    private int combineHash(int existing, int newValue) {
        return 31 * existing + newValue;
    }

    public Map<String, EmoteFileInfo> getFileInfoMap() {
        return Collections.unmodifiableMap(fileInfoMap);
    }
}
