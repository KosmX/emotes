package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Avatar;
import net.raphimc.noteblocklib.format.nbs.model.event.NbsSoundStopperEvent;
import net.raphimc.noteblocklib.model.note.Note;
import net.raphimc.noteblocklib.model.song.Song;
import net.raphimc.noteblocklib.util.TimerHack;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MinecraftNbsPlayer extends NbsPlayer {
    private final Queue<PlayingNote> playingNotes = new ConcurrentLinkedQueue<>();
    private final boolean hasSoundStoppers;

    protected final Avatar avatar;

    public MinecraftNbsPlayer(PlayerAnimationController controller, Song song) {
        super(song, controller);
        this.avatar = controller.getAvatar();
        this.hasSoundStoppers = song.getEvents().testEach(NbsSoundStopperEvent.class::isInstance);
    }

    @Override
    public void start(int delay, int tick) {
        TimerHack.ENABLED = false;
        super.start(delay, tick);
    }

    @Override
    protected boolean shouldTick() {
        if (this.avatar instanceof UnsafeMannequin) return super.shouldTick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || this.avatar.isRemoved()) {
            stop();
            return false;
        }
        return !mc.isPaused() && !this.avatar.isInvisibleTo(mc.player) && super.shouldTick();
    }

    @Override
    protected void playNote(Note note) {
        SoundInstance sound = InstrumentConventer.getInstrument(note, this.avatar.position());
        if (this.hasSoundStoppers) this.playingNotes.add(new PlayingNote(note, sound));
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.getSoundManager().play(sound));
    }

    @Override
    protected void handleEvent(NbsSoundStopperEvent event) {
        Minecraft mc = Minecraft.getInstance();
        this.playingNotes.removeIf(playing -> {
            if (!event.shouldStop(playing.note())) return false;
            mc.execute(() -> mc.getSoundManager().stop(playing.sound()));
            return true;
        });
    }

    @Override
    public void stop() {
        super.stop();
        this.playingNotes.clear();
    }

    private record PlayingNote(Note note, SoundInstance sound) {}
}
