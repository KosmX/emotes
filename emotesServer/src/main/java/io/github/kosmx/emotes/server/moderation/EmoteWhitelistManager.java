package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the emote whitelist for server-side moderation.
 * This class handles reading the whitelist file and checking if emotes are allowed.
 */
public class EmoteWhitelistManager {
    private static final EmoteWhitelistManager INSTANCE = new EmoteWhitelistManager();
    
    private Set<String> whitelistedContentHashes = new HashSet<>();
    
    private EmoteWhitelistManager() {
        loadWhitelist();
    }
    
    public static EmoteWhitelistManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Check if whitelist moderation is enabled
     */
    public boolean isWhitelistEnabled() {
        return Serializer.getConfig().enableEmoteWhitelist.get();
    }
    
    /**
     * Check if an emote is allowed according to the whitelist
     * Uses content hash (boneAnimations hashCode) for checking instead of UUID.
     * @param emote The emote to check
     * @return true if the emote is allowed, false otherwise
     */
    /**
     * Check if an emote is allowed according to the whitelist, with player context
     * @param emote The emote to check
     * @param playerUuid The UUID of the player who triggered the emote
     * @return true if the emote is allowed, false otherwise
     */
    public boolean isEmoteAllowed(Animation emote, java.util.UUID playerUuid) {
        long validationStartTime = System.nanoTime();
        if (!isWhitelistEnabled()) {
            return true; // If whitelist is disabled, allow all emotes
        }
        String contentHash = generateContentHash(emote);
        if (whitelistedContentHashes.contains(contentHash)) {
            CommonData.LOGGER.info("Emote {} ALLOWED by hash {}", emote.uuid(), contentHash);
            long totalValidationTime = System.nanoTime() - validationStartTime;
            CommonData.LOGGER.info("Total emote validation time: {} ms", String.format("%.3f", totalValidationTime / 1_000_000.0));
            return true;
        }
        long totalValidationTime = System.nanoTime() - validationStartTime;
        CommonData.LOGGER.info("Total emote validation time: {} ms", String.format("%.3f", totalValidationTime / 1_000_000.0));
        String playerName = resolvePlayerName(playerUuid);
        CommonData.LOGGER.info("Emote with hash {} was run by player {} - {} because the emote is not in the whitelist.", contentHash, playerName, "\u001B[31mdenied\u001B[0m");
        return false;
    }

    /**
     * Try to resolve the player name from UUID using the Minecraft server API (for online players).
     * Returns the UUID as a string if the name cannot be found.
     */
    private String resolvePlayerName(java.util.UUID uuid) {
        try {
            // Try Paper/Spigot/Bukkit API first
            Class<?> serverClass = Class.forName("org.bukkit.Bukkit");
            Object player = serverClass.getMethod("getPlayer", java.util.UUID.class).invoke(null, uuid);
            if (player != null) {
                String name = (String) player.getClass().getMethod("getName").invoke(player);
                if (name != null) return name;
            }
        } catch (Throwable ignored) {}
        try {
            // Try Fabric/Forge (MinecraftServer API)
            Class<?> serverClass = Class.forName("net.minecraft.server.MinecraftServer");
            Object server = serverClass.getMethod("getServer").invoke(null);
            Object playerList = server.getClass().getMethod("getPlayerList").invoke(server);
            Object player = playerList.getClass().getMethod("getPlayer", java.util.UUID.class).invoke(playerList, uuid);
            if (player != null) {
                Object nameObj = player.getClass().getMethod("getName").invoke(player);
                if (nameObj != null) return nameObj.toString();
            }
        } catch (Throwable ignored) {}
        return uuid.toString();
    }

    
    /**
     * Load the whitelist from file
     */
    public void loadWhitelist() {
        long loadStartTime = System.nanoTime();
        
        long clearStartTime = System.nanoTime();
        whitelistedContentHashes.clear();
        long clearTime = System.nanoTime() - clearStartTime;
        
        long pathGetStartTime = System.nanoTime();
        Path whitelistPath = getWhitelistPath();
        long pathGetTime = System.nanoTime() - pathGetStartTime;
        
        if (!Files.exists(whitelistPath)) {
            long createStartTime = System.nanoTime();
            createDefaultWhitelist(whitelistPath);
            return;
        }
        
        try {
            long fileReadStartTime = System.nanoTime();
            List<String> lines = Files.readAllLines(whitelistPath);
            long fileReadTime = System.nanoTime() - fileReadStartTime;

            long processStartTime = System.nanoTime();
            for (String line : lines) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Check if it's a content hash (integer value)
                try {
                    // Try to parse as integer (content hash)
                    Integer.parseInt(line);
                    whitelistedContentHashes.add(line);
                } catch (NumberFormatException e) {
                    CommonData.LOGGER.warn("Invalid entry in whitelist, skipping: {}", line);
                }
            }
            long processTime = System.nanoTime() - processStartTime;
            
            long totalTime = System.nanoTime() - loadStartTime;
            
            CommonData.LOGGER.debug("Whitelist Load Breakdown - Clear: {} μs, Path: {} μs, FileRead: {} μs, Process: {} μs, Total: {} μs (Loaded {} hashes)", 
                String.format("%.3f", clearTime / 1000.0),
                String.format("%.3f", pathGetTime / 1000.0),
                String.format("%.3f", fileReadTime / 1000.0),
                String.format("%.3f", processTime / 1000.0),
                String.format("%.3f", totalTime / 1000.0),
                whitelistedContentHashes.size());
            
            CommonData.LOGGER.info("Loaded {} content hashes from whitelist", whitelistedContentHashes.size());
            
        } catch (IOException e) {
            CommonData.LOGGER.error("Failed to load emote whitelist from {}", whitelistPath, e);
        }
    }

    /**
     * Add a hash to the whitelist, with an optional label (as a comment).
     * Updates both the in-memory set and the file.
     */
    public synchronized boolean addHashToWhitelist(String hash, String label) {
        if (whitelistedContentHashes.contains(hash)) return false;
        whitelistedContentHashes.add(hash);
        Path whitelistPath = getWhitelistPath();
        try {
            List<String> lines = Files.exists(whitelistPath) ? Files.readAllLines(whitelistPath) : new java.util.ArrayList<>();
            String entry = hash + (label != null && !label.isEmpty() ? " # " + label : "");
            lines.add(entry);
            Files.write(whitelistPath, lines);
            CommonData.LOGGER.info("Added hash {} to whitelist{}", hash, (label != null && !label.isEmpty() ? " (" + label + ")" : ""));
            return true;
        } catch (IOException e) {
            CommonData.LOGGER.error("Failed to add hash {} to whitelist file", hash, e);
            return false;
        }
    }

    /**
     * Remove a hash from the whitelist (in-memory and file).
     */
    public synchronized boolean removeHashFromWhitelist(String hash) {
        if (!whitelistedContentHashes.contains(hash)) return false;
        whitelistedContentHashes.remove(hash);
        Path whitelistPath = getWhitelistPath();
        try {
            List<String> lines = Files.exists(whitelistPath) ? Files.readAllLines(whitelistPath) : new java.util.ArrayList<>();
            List<String> newLines = new java.util.ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    newLines.add(line);
                    continue;
                }
                String fileHash = trimmed.split("\\s", 2)[0];
                if (!fileHash.equals(hash)) {
                    newLines.add(line);
                }
            }
            Files.write(whitelistPath, newLines);
            CommonData.LOGGER.info("Removed hash {} from whitelist", hash);
            return true;
        } catch (IOException e) {
            CommonData.LOGGER.error("Failed to remove hash {} from whitelist file", hash, e);
            return false;
        }
    }
    
    /**
     * Generate a content-based hash for an emote that includes core playback-affecting components.
     * This provides a more secure hash than the original UUID algorithm which only uses boneAnimations.
     * Includes: bone animations, sound effects, particle effects, and loop behavior.
     */
    private String generateContentHash(Animation emote) {
        int hash = calculateHash(emote);
        return String.valueOf(hash);
    }
    
    /**
     * Calculate the actual hash value from all emote components
     * Only includes dynamic content that affects playback, not derived/static properties
     */
    private int calculateHash(Animation emote) {
        // Bone animations (base hash)
        int hash = emote.boneAnimations().hashCode();
        // Sound keyframes
        for (var sound : emote.keyFrames().sounds()) {
            hash = combineHash(hash, sound.hashCode());
        }
        // Particle keyframes
        for (var particle : emote.keyFrames().particles()) {
            hash = combineHash(hash, particle.hashCode());
        }
        // Loop behavior (affects playback)
        hash = combineHash(hash, emote.loopType().getClass().hashCode());
        return hash;
    }
    
    /**
     * Combine two hash values using the standard approach
     */
    private int combineHash(int existing, int newValue) {
        return 31 * existing + newValue;
    }
    
    /**
     * Check if whitelist file has been modified and reload if necessary
     */
    
    /**
     * Get the path to the whitelist file
     */
    private Path getWhitelistPath() {
        String whitelistFileName = Serializer.getConfig().emoteWhitelistPath.get();
        return InstanceService.INSTANCE.getConfigPath().getParent().resolve(whitelistFileName);
    }
    
    /**
     * Create a default whitelist file with some examples
     */
    private void createDefaultWhitelist(Path whitelistPath) {
        try {
            // Ensure parent directory exists
            Files.createDirectories(whitelistPath.getParent());
            
            StringBuilder content = new StringBuilder();
            content.append("# Emotecraft Server Whitelist\n");
            content.append("# This file contains content hashes of emotes that are allowed on this server.\n");
            content.append("# Lines starting with # are comments and will be ignored.\n");
            content.append("# \n");
            content.append("# Content hashes include core playback-affecting components:\n");
            content.append("# - Bone animations (movement)\n");
            content.append("# - Sound effects (NBS files, audio)\n");
            content.append("# - Particle effects\n");
            content.append("# - Loop behavior\n");
            content.append("# \n");
            content.append("# This provides more security than UUID-only checking and prevents\n");
            content.append("# emotes with malicious sounds/particles from being allowed.\n");
            content.append("# \n");
            content.append("# To find content hashes:\n");
            content.append("# 1. Check server logs when emotes are used\n");
            content.append("# 2. The content hash will be shown in debug output\n");
            content.append("# 3. Add the hash value here (one per line) to allow that emote\n");
            content.append("# \n");
            content.append("# Example content hash:\n");
            content.append("# 1234567890\n");
            content.append("\n");
            content.append("# Add your allowed emote content hashes below:\n");
            
            Files.write(whitelistPath, content.toString().getBytes());
            
            CommonData.LOGGER.info("Created default whitelist file at {}", whitelistPath);
            
            // Load the newly created whitelist
            loadWhitelist();
            
        } catch (IOException e) {
            CommonData.LOGGER.error("Failed to create default whitelist file at {}", whitelistPath, e);
        }
    }
    
    /**
     * Get the denial message to send to the player
     */
    /**
     * Get the denial message to send to the player, including the emote hash.
     * @param hash The emote content hash
     * @return The denial message with the hash value
     */
    public String getDenialMessage(String hash) {
        return "This emote is currentely disabeled on this server (Emote Hash: " + hash + ")";
    }
    
    /**
     * Get the content hash for an emote (for administrative use).
     * This can be used to create content hash entries in the whitelist
     * that will survive PlayerAnimationLibrary UUID changes.
     */
    public String getContentHashForEmote(Animation emote) {
        return generateContentHash(emote);
    }
    
}
