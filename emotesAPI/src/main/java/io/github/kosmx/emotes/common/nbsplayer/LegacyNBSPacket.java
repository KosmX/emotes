package io.github.kosmx.emotes.common.nbsplayer;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.NbsDefinitions;
import net.raphimc.noteblocklib.format.nbs.model.NbsLayer;
import net.raphimc.noteblocklib.format.nbs.model.NbsNote;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.Note;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

@Deprecated
public class LegacyNBSPacket {
    NbsSong song;
    int version = 1;
    final int packetVersion = 1;

    boolean valid = true;

    public LegacyNBSPacket(NbsSong song) {
        this.song = song;
    }

    public LegacyNBSPacket() {

    }

    public NbsSong getSong() {
        return song;
    }

    public void write(ByteBuffer buf){
        buf.putInt(packetVersion); //reserved for later use/changes
        buf.put((byte) 0);
        buf.put((byte) song.getVanillaInstrumentCount());
        buf.putShort(song.getTempo()); //that one is important;
        buf.put(song.getTimeSignature());
        CommonNetwork.writeBoolean(buf, song.isLoop());
        buf.put(song.getMaxLoopCount());
        buf.putShort(song.getLoopStartTick());
        buf.putShort((short) song.getLayers().size());
        writeLayersAndNotes(buf);
    }

    public void writeLayersAndNotes(ByteBuffer buf){
        for (Map.Entry<Integer, NbsLayer> layerEntry : song.getLayers().entrySet()) {
            NbsLayer layer = layerEntry.getValue();
            buf.put(layer.getVolume());
            buf.put((byte) layer.getPanning());
            int tick = -1;
            for (Map.Entry<Integer, NbsNote> noteEntry : layer.getNotes().entrySet()) {
                NbsNote note = noteEntry.getValue();
                buf.putShort((short) (noteEntry.getKey() - tick));
                tick = noteEntry.getKey(); //before I forget it
                buf.put((byte) note.getInstrument());
                buf.put(note.getKey());
                buf.put(note.getVelocity());
                buf.put((byte) note.getPanning());
                buf.putShort(note.getPitch());
            }
            buf.putShort((short) 0);//end of the notes
        }
    }

    /**
     *
     * @param buf input ByteBuf
     * @return true if reading was success
     */
    public boolean read(ByteBuffer buf) throws IOException {
        version = buf.getInt();
        buf.get(); // sendExtraData
        NbsSong builder = new NbsSong();
        builder.setVersion((byte) 5);

        builder.setVanillaInstrumentCount(buf.get());
        builder.setTempo(buf.getShort());
        builder.setTimeSignature(buf.get());
        builder.setLoop(CommonNetwork.readBoolean(buf));
        builder.setMaxLoopCount(buf.get());
        builder.setLoopStartTick(buf.getShort());

        builder.setLayerCount(buf.getShort());

        this.song = builder;
        readLayersAndNotes(buf);

        return valid;
    }

    void readLayersAndNotes(ByteBuffer buf) {
        Map<Integer, NbsLayer> layers = this.song.getLayers();
        if (song.getLayerCount() != layers.size()) {
            if (!layers.isEmpty()) {
                this.valid = false;
                return;
            }

            for (int i = 0; i < song.getLayerCount(); i++) {
                layers.put(i, new NbsLayer());
            }
        }

        int length = 0;
        for(Map.Entry<Integer, NbsLayer> layerEntry : layers.entrySet()) { //Layers are existing but not configured.
            NbsLayer layer = layerEntry.getValue();
            layer.setVolume(buf.get());
            layer.setPanning(buf.get());

            int tick = -1;
            for(int step = buf.getShort(); step != 0; step = buf.getShort()){
                tick += step;

                NbsNote note = new NbsNote();
                note.setInstrument(buf.get());
                note.setKey(buf.get());
                note.setVelocity(buf.get());
                note.setPanning(buf.get());
                note.setPitch(buf.getShort());
                layer.getNotes().put(tick, note);

                length = Math.max(length, tick);
            }
        }
        this.song.setLength((short) length);

        { // Fill generalized song structure with data
            song.getTempoEvents().set(0, song.getTempo() / 100F);
            for (NbsLayer layer : layers.values()) {
                for (Map.Entry<Integer, NbsNote> noteEntry : layer.getNotes().entrySet()) {
                    final NbsNote nbsNote = noteEntry.getValue();

                    final Note note = new Note();
                    note.setNbsKey((float) NbsDefinitions.getEffectivePitch(nbsNote) / NbsDefinitions.PITCHES_PER_KEY);
                    note.setVolume((layer.getVolume() / 100F) * (nbsNote.getVelocity() / 100F));
                    if (layer.getPanning() == NbsDefinitions.CENTER_PANNING) { // Special case
                        note.setPanning((nbsNote.getPanning() - NbsDefinitions.CENTER_PANNING) / 100F);
                    } else {
                        note.setPanning(((layer.getPanning() - NbsDefinitions.CENTER_PANNING) + (nbsNote.getPanning() - NbsDefinitions.CENTER_PANNING)) / 200F);
                    }

                    if (nbsNote.getInstrument() < song.getVanillaInstrumentCount()) {
                        note.setInstrument(MinecraftInstrument.fromNbsId((byte) nbsNote.getInstrument()));
                    }

                    song.getNotes().add(noteEntry.getKey(), note);
                }
            }
        }
    }

    /**
     * Warning! Works incorrectly when sending extra data
     * @param song song to send
     * @return estimated size
     */
    public static int calculateMessageSize(NbsSong song) {
        int size = 15;
        //Always IBBSBBBSS
        //extra S;string;string;string;string;BBIIIII;string
        //I won't ever send extra
        for(NbsLayer layer : song.getLayers().values()){
            size += getLayerMessageSize(layer);
        }

        return size;
    }

    public static int getLayerMessageSize(NbsLayer layer) {
        //Layer static size BBS
        //note size SBBBBS
        return 4 + layer.getNotes().size()*8;
    }
}
