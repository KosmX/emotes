package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.TimerHack;
import org.jetbrains.annotations.Nullable;

public class MinecraftNbsPlayer extends NbsPlayer {
    protected final AbstractClientPlayer player;

    public MinecraftNbsPlayer(AbstractClientPlayer player, Song song) {
        super(song);
        this.player = player;
    }

    @Override
    public void start(int delay, int tick) {
        TimerHack.ENABLED = false;
        super.start(delay, tick);
    }

    @Override
    protected boolean preTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!(this.player instanceof UnsafeRemotePlayer) && mc.level != this.player.level()) {
            stop();
            return false;
        }
        return !mc.isPaused();
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

    @Override
    protected void playNote(Note note) {
        Minecraft.getInstance().execute(() -> this.player.emotecraft$playRawSound(
                InstrumentConventer.getInstrument(note, this.player.position())
        ));
    }
}
