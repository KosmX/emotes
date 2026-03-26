package io.github.kosmx.emotes.common.nbsplayer;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.netty.buffer.ByteBuf;
import net.raphimc.noteblocklib.format.midi.MidiDefinitions;
import net.raphimc.noteblocklib.format.minecraft.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.model.NbsLayer;
import net.raphimc.noteblocklib.format.nbs.model.NbsNote;
import net.raphimc.noteblocklib.format.nbs.model.NbsSong;
import net.raphimc.noteblocklib.model.note.Note;
import net.raphimc.noteblocklib.util.MathUtil;

import java.io.IOException;
import java.util.Map;

import static net.raphimc.noteblocklib.format.nbs.NbsDefinitions.*;

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
            final boolean hasSoloLayers = layers.values().stream().anyMatch(layer -> layer.getStatus() == NbsLayer.Status.SOLO);
            for (Map.Entry<Integer, NbsLayer> entry : layers.entrySet()) {
                final NbsLayer layer = entry.getValue();
                for (Map.Entry<Integer, NbsNote> noteEntry : layer.getNotes().entrySet()) {
                    final NbsNote nbsNote = noteEntry.getValue();

                    final Note note = new Note();
                    note.setGroupId(entry.getKey());
                    final float effectiveKey = (float) (MathUtil.clamp(nbsNote.getKey(), LOWEST_KEY, HIGHEST_KEY) * PITCHES_PER_KEY + nbsNote.getPitch()) / PITCHES_PER_KEY;
                    note.setMidiKey(MathUtil.clamp(LOWEST_MIDI_KEY + effectiveKey, MidiDefinitions.LOWEST_KEY, MidiDefinitions.HIGHEST_KEY));

                    if (nbsNote.getInstrument() < song.getVanillaInstrumentCount()) {
                        note.setInstrument(MinecraftInstrument.fromNbsId(nbsNote.getInstrument()));
                    } else {
                        note.setInstrument(MinecraftInstrument.BANJO);
                        note.setVolume(0F); // Mute custom cuz not supported
                    }

                    note.setVolume(MathUtil.clamp(Math.min(layer.getVolume() / 100F, 1F) * (nbsNote.getVelocity() / 100F), 0F, 1F));
                    if (layer.getPanning() == CENTER_PANNING) { // Special case
                        note.setPanning(MathUtil.clamp((nbsNote.getPanning() - CENTER_PANNING) / 100F, -1F, 1F));
                    } else {
                        note.setPanning(MathUtil.clamp(((layer.getPanning() - CENTER_PANNING) + (nbsNote.getPanning() - CENTER_PANNING)) / 200F, -1F, 1F));
                    }

                    if (layer.getStatus() == NbsLayer.Status.LOCKED) { // Locked layers are muted
                        note.setVolume(0F);
                    } else if (hasSoloLayers && layer.getStatus() != NbsLayer.Status.SOLO) { // Non-solo layers are muted if there are solo layers
                        note.setVolume(0F);
                    }

                    song.getNotes().add(noteEntry.getKey(), note);
                }
            }
        }
    }
}
