package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.TimerHack;
import org.jetbrains.annotations.Nullable;

public class MinecraftNbsPlayer extends NbsPlayer {
    protected final AbstractClientPlayer player;

    public MinecraftNbsPlayer(PlayerAnimationController controller, Song song) {
        super(song, controller);
        this.player = controller.getPlayer();
    }

    @Override
    public void start(int delay, int tick) {
        TimerHack.ENABLED = false;
        super.start(delay, tick);
    }

    @Override
    protected boolean preTick() {
        if (this.player instanceof UnsafeRemotePlayer) return super.preTick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != this.player.level()) {
            stop();
            return false;
        }
        return !mc.isPaused() && super.preTick();
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
        SoundInstance sound = InstrumentConventer.getInstrument(note, this.player.position());
        Minecraft.getInstance().execute(() -> this.player.emotecraft$playRawSound(sound));
    }
}
