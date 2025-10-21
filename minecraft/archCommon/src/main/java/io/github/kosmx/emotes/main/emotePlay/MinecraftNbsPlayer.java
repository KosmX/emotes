package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.TimerHack;
import org.jetbrains.annotations.Nullable;

public class MinecraftNbsPlayer extends NbsPlayer {
    protected final Avatar avatar;

    public MinecraftNbsPlayer(PlayerAnimationController controller, Song song) {
        super(song, controller);
        this.avatar = controller.getAvatar();
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
        if (mc.level != this.avatar.level()) {
            stop();
            return false;
        }
        return !mc.isPaused() && super.shouldTick();
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
        SoundInstance sound = InstrumentConventer.getInstrument(note, this.avatar.position());
        Minecraft.getInstance().execute(() -> this.avatar.emotecraft$playRawSound(sound));
    }
}
