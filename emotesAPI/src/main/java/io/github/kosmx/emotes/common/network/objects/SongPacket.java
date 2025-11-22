package io.github.kosmx.emotes.common.network.objects;

import io.github.kosmx.emotes.common.nbsplayer.LegacyNBSPacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.format.SongFormat;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Song;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("deprecation")
public class SongPacket extends AbstractNetworkPacket {
    @Override
    public byte getID() {
        return PacketConfig.NBS_CONFIG;
    }

    @Override
    public byte getVer() {
        return 2; // Ver0 means NO sound
    }

    @Override
    public void read(ByteBuf buf, NetData config, byte version) throws IOException {
        Song song = switch (version) {
            case 2 -> {
                try (InputStream is = new ByteBufInputStream(buf)) {
                    yield NoteBlockLib.readSong(is, SongFormat.NBS);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            case 1 -> LegacyNBSPacket.read(buf);
            default -> null;
        };
        config.extraData.put("song", song);
    }

    @Override
    public void write(ByteBuf buf, NetData config, byte version) throws IOException {
        assert config.emoteData != null;

        Song song = (Song) config.emoteData.data().getRaw("song");
        if (version > 1) {
            try (OutputStream os = new ByteBufOutputStream(buf)) {
                NoteBlockLib.writeSong(song, os);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            LegacyNBSPacket.write((NbsSong) song, buf);
        }
    }

    @Override
    public boolean doWrite(NetData config) {
        return config.versions.get(this.getID()) != 0 && config.emoteData != null && config.emoteData.data().has("song");
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
