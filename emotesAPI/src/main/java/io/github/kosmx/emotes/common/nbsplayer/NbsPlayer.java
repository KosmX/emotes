package io.github.kosmx.emotes.common.nbsplayer;

import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.SongPlayer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class NbsPlayer extends SongPlayer {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5,
            Thread.ofVirtual().name("Emotecraft-NBSplayer-", 0).factory()
    );

    private final Consumer<Note> noteConsumer;
    protected int loopCount = 0;

    public NbsPlayer(Song song, Consumer<Note> noteConsumer, int tick) {
        super(song);
        setTick(tick);
        setCustomScheduler(EXECUTOR);
        this.noteConsumer = noteConsumer;
    }

    @Override
    protected void playNotes(List<Note> notes) {
        for (Note note : notes) this.noteConsumer.accept(note);
    }

    @Override
    protected void onFinished() {
        if (getSong() instanceof NbsSong nbsSong) {
            if (nbsSong.isLoop() && (this.loopCount < nbsSong.getMaxLoopCount() || nbsSong.getMaxLoopCount() == 0)) {
                this.loopCount++;
                this.start((int) (1000 / this.getCurrentTicksPerSecond()), nbsSong.getLoopStartTick());
            }
        }
    }
}
