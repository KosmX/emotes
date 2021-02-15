package com.kosmx.opennbs;

import com.kosmx.opennbs.format.Layer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Tickable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Plays {@link NBS} objects
 * It will need to be played on ClientSide
 */
@Environment(EnvType.CLIENT)
public class SoundPlayer implements Tickable {
    final NBS song;
    final float songPerMCTick;
    int mcTick = 0; //MC tick (20 tps) not the song's custom
    int soundTick = 0; //MCTick * (song tickspeed/MCTickspeed)
    boolean isPlaying = true; //set false, when stopped. Newer set to true after stopped, instead create a new player.
    //To play the song in some interesting ways
    final Consumer<Layer.Note> playSound;

    public SoundPlayer(NBS song, Consumer<Layer.Note> soundPlayer) {
        this.song = song;
        this.songPerMCTick = ((float) song.header.Song_tempo) / 100f;
        this.playSound = soundPlayer;
    }

    /**
     * Tickable for some use...
     */
    @Override
    public void tick(){
        this.mcTick++;
        int newSongTick = (int) (mcTick * songPerMCTick);
        if(newSongTick == this.soundTick){
            return; //Nothing has happened, can continue;
        }
        else if(newSongTick >= song.getLength()){
            newSongTick = 0;
        }
        List<Layer.Note> notesToPlay = this.song.getNotesUntilTick(this.soundTick, newSongTick);
        //MinecraftClient.getInstance().world.playSoundFromEntity();
        notesToPlay.forEach(this.playSound);


    }

    public void stop(){
        this.isPlaying = false;
    }

    //My favorite one :D
    public boolean isPlayingSong(@Nullable SoundPlayer player){
        return player != null && player.isPlaying;
    }
}
