package io.github.kosmx.emotes.common.nbsplayer;

import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.SongPlayer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class NbsPlayer extends SongPlayer {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5,
            Thread.ofVirtual().name("Emotecraft-NBSplayer-", 0).factory()
    );

    protected int loopCount = 0;
    private boolean firstSongPlayed;

    public NbsPlayer(Song song) {
        super(song);
        setCustomScheduler(EXECUTOR);
    }

    @Override
    protected void playNotes(List<Note> notes) {
        this.firstSongPlayed = true;
        for (Note note : notes) playNote(note);
    }

    protected abstract void playNote(Note note);

    @Override
    protected void onFinished() {
        if (getSong() instanceof NbsSong nbsSong) {
            if (nbsSong.isLoop() && (this.loopCount < nbsSong.getMaxLoopCount() || nbsSong.getMaxLoopCount() == 0)) {
                this.loopCount++;
                this.start((int) (1000 / this.getCurrentTicksPerSecond()), nbsSong.getLoopStartTick());
            }
        }
    }

    public boolean isFirstSongPlayed() {
        return this.firstSongPlayed;
    }
}
