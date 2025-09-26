package io.github.kosmx.emotes.common.nbsplayer;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.enums.State;
import io.github.kosmx.emotes.common.CommonData;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.SongPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class NbsPlayer extends SongPlayer {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5,
            Thread.ofVirtual().name("Emotecraft-NBSplayer-", 0).factory()
    );

    @Nullable
    protected final AnimationController controller;

    protected int loopCount = 0;
    private boolean firstSongPlayed;

    public NbsPlayer(Song song, @Nullable AnimationController controller) {
        super(song);
        this.controller = controller;
        setCustomScheduler(EXECUTOR);
    }

    @Override
    protected void playNotes(List<Note> notes) {
        this.firstSongPlayed = true;
        for (Note note : notes) playNote(note);
    }

    @Override
    protected boolean shouldTick() {
        if (this.controller == null) return true;

        if (!this.controller.isActive()) {
            stop();
            return false;
        }
        return this.controller.getAnimationState() == State.RUNNING;
    }

    protected abstract void playNote(Note note);

    @Override
    protected void onSongFinished() {
        super.onSongFinished();

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

    @Override
    protected void onTickException(Throwable e) {
        CommonData.LOGGER.warn("An error occurred while playing nbs!", e);
        stop();
    }
}
