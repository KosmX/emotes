package io.github.kosmx.emotes.common.nbsplayer;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.netty.buffer.ByteBuf;
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
    public static void write(NbsSong song, ByteBuf buf) {
        buf.writeInt(1); //reserved for later use/changes
        buf.writeByte((byte) 0);
        buf.writeByte((byte) song.getVanillaInstrumentCount());
        buf.writeShort(song.getTempo()); //that one is important;
        buf.writeByte(song.getTimeSignature());
        CommonNetwork.writeBoolean(buf, song.isLoop());
        buf.writeByte(song.getMaxLoopCount());
        buf.writeShort(song.getLoopStartTick());
        buf.writeShort((short) song.getLayers().size());
        writeLayersAndNotes(song, buf);
    }

    private static void writeLayersAndNotes(NbsSong song, ByteBuf buf) {
        for (Map.Entry<Integer, NbsLayer> layerEntry : song.getLayers().entrySet()) {
            NbsLayer layer = layerEntry.getValue();
            buf.writeByte(layer.getVolume());
            buf.writeByte((byte) layer.getPanning());
            int tick = -1;
            for (Map.Entry<Integer, NbsNote> noteEntry : layer.getNotes().entrySet()) {
                NbsNote note = noteEntry.getValue();
                buf.writeShort((short) (noteEntry.getKey() - tick));
                tick = noteEntry.getKey(); //before I forget it
                buf.writeByte((byte) note.getInstrument());
                buf.writeByte(note.getKey());
                buf.writeByte(note.getVelocity());
                buf.writeByte((byte) note.getPanning());
                buf.writeShort(note.getPitch());
            }
            buf.writeShort((short) 0);//end of the notes
        }
    }

    /**
     *
     * @param buf input ByteBuf
     * @return nbs song
     */
    public static NbsSong read(ByteBuf buf) throws IOException {
        buf.readInt(); // version
        buf.readByte(); // sendExtraData
        NbsSong builder = new NbsSong();
        builder.setVersion((byte) 5);

        builder.setVanillaInstrumentCount(buf.readByte());
        builder.setTempo(buf.readShort());
        builder.setTimeSignature(buf.readByte());
        builder.setLoop(CommonNetwork.readBoolean(buf));
        builder.setMaxLoopCount(buf.readByte());
        builder.setLoopStartTick(buf.readShort());

        builder.setLayerCount(buf.readShort());

        readLayersAndNotes(builder, buf);
        return builder;
    }

    private static void readLayersAndNotes(NbsSong song, ByteBuf buf) {
        Map<Integer, NbsLayer> layers = song.getLayers();
        if (song.getLayerCount() != layers.size()) {
            if (!layers.isEmpty()) {
                return;
            }

            for (int i = 0; i < song.getLayerCount(); i++) {
                layers.put(i, new NbsLayer());
            }
        }

        int length = 0;
        for(Map.Entry<Integer, NbsLayer> layerEntry : layers.entrySet()) { //Layers are existing but not configured.
            NbsLayer layer = layerEntry.getValue();
            layer.setVolume(buf.readByte());
            layer.setPanning(buf.readByte());

            int tick = -1;
            for(int step = buf.readShort(); step != 0; step = buf.readShort()){
                tick += step;

                NbsNote note = new NbsNote();
                note.setInstrument(buf.readByte());
                note.setKey(buf.readByte());
                note.setVelocity(buf.readByte());
                note.setPanning(buf.readByte());
                note.setPitch(buf.readShort());
                layer.getNotes().put(tick, note);

                length = Math.max(length, tick);
            }
        }
        song.setLength((short) length);

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
}
