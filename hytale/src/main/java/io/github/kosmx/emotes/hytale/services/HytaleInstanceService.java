package io.github.kosmx.emotes.hytale.services;

import io.github.kosmx.emotes.server.services.InstanceService;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Roots Emotecraft's directories at the plugin's own data directory rather than the server's working directory.
 * <p>
 * The fallback implementation resolves everything against {@code Paths.get("")}, which on a Hytale server would scatter
 * the config and the emote cache across the server root. Hytale hands every plugin a directory of its own, so
 * {@link #setDataDirectory} points this service at it as soon as the plugin starts.
 */
public final class HytaleInstanceService implements InstanceService {
    private static volatile Path dataDirectory;

    /** Called from the plugin's constructor, before anything reads a path. */
    public static void setDataDirectory(Path directory) {
        dataDirectory = directory;
    }

    @Override
    public Path getGameDirectory() {
        Path directory = dataDirectory;
        return directory != null ? directory : Paths.get("");
    }

    @Override
    public Path getCacheDirectory() {
        // The default nests a directory named after the mod inside the game directory, which here already is one.
        return getGameDirectory().resolve("cache");
    }

    @Override
    public int getPriority() {
        return 0; // above InstanceServiceImpl's LOWEST_PRIORITY, so this one wins
    }

    @Override
    public boolean isServiceActive() {
        return true;
    }
}
