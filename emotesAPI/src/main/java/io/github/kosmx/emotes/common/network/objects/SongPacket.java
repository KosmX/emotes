package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.nbsplayer.LegacyNBSPacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.tools.ByteBufferInputStream;
import io.github.kosmx.emotes.common.tools.ByteBufferOutputStream;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.format.SongFormat;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Song;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SongPacket extends AbstractNetworkPacket{
    @Override
    public byte getID() {
        return PacketConfig.NBS_CONFIG;
    }

    @Override
    public byte getVer() {
        return 2; // Ver0 means NO sound
    }

    @Override
    public void read(ByteBuffer byteBuffer, NetData config, int version) throws IOException {
        Song song = switch (version) {
            case 2 -> {
                try {
                    yield NoteBlockLib.readSong(new ByteBufferInputStream(byteBuffer), SongFormat.NBS);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            case 1 -> {
                LegacyNBSPacket reader = new LegacyNBSPacket();
                reader.read(byteBuffer);
                yield reader.getSong();
            }

            default -> null;
        };
        config.extraData.put("song", song);
    }

    @Override
    public void write(ByteBuffer byteBuffer, NetData config) throws IOException {
        assert config.emoteData != null;

        Song song = (Song) config.emoteData.extraData.get("song");
        int version = getVer(config.versions);

        if (version > 1) {
            try {
                NoteBlockLib.writeSong(song, new ByteBufferOutputStream(byteBuffer));
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            LegacyNBSPacket writer = new LegacyNBSPacket((NbsSong) song);
            writer.write(byteBuffer);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.versions.get(this.getID()) != 0 && config.emoteData != null && config.emoteData.extraData.containsKey("song") && config.writeSong;
    }

    @Override
    public int calculateSize(NetData config) {
        if (config.emoteData == null || config.emoteData.extraData.get("song") == null) return 0;
        Song song = (Song) config.emoteData.extraData.get("song");
        if (getVer(config.versions) > 1) {
            return calculateSongSize(song);
        } else {
            return LegacyNBSPacket.calculateMessageSize((NbsSong) song);
        }
    }

    public static int calculateSongSize(Song song) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NoteBlockLib.writeSong(song, os);
            return os.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
