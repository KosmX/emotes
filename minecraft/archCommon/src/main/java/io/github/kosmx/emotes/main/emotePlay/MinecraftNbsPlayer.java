package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.TimerHack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinecraftNbsPlayer extends NbsPlayer {
    public MinecraftNbsPlayer(Song song, Consumer<Note> noteConsumer, int tick) {
        super(song, noteConsumer, tick);
    }

    @Override
    public void start(int delay, int tick) {
        TimerHack.ENABLED = false;
        super.start(delay, tick);
    }

    @Override
    protected boolean preTick() {
        return !Minecraft.getInstance().isPaused();
    }

    public @Nullable Component getNowPlaying() {
        String author = getSong().getAuthorOr(getSong().getOriginalAuthorOr(""));
        String name = getSong().getTitleOrFileNameOr("");

        if (author.isEmpty()) {
            if (!name.isEmpty()) {
                return Component.literal(name);
            } else {
                return null;
            }
        } else if (!name.isEmpty()) {
            return Component.literal(String.format("%s - %s", author, name));
        }

        return null;
    }
}
