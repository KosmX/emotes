package io.github.kosmx.emotes.common.opennbs.network;

import io.github.kosmx.emotes.common.network.CommonNetwork;
import io.github.kosmx.emotes.common.opennbs.NBS;
import io.github.kosmx.emotes.common.opennbs.format.Header;
import io.github.kosmx.emotes.common.opennbs.format.Layer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NBSPacket {
    NBS song;
    boolean sendExtraData = false; //true if send/receive name, author and other not important data to play the song
    int version = 1;
    final int packetVersion = 1;

    boolean valid = true;

    public NBSPacket(NBS song){
        this.song = song;
    }

    public NBSPacket(){

    }

    public NBS getSong() {
        return song;
    }

    public void write(ByteBuffer buf){
        buf.putInt(packetVersion); //reserved for later use/changes
        buf.put((byte) (sendExtraData ? 1 : 0));
        buf.put(song.header.Vanilla_instrument_count);
        if(sendExtraData){
            buf.putShort(song.header.Song_length);
            CommonNetwork.writeString(buf, song.header.Song_name);
            CommonNetwork.writeString(buf, song.header.Song_author);
            CommonNetwork.writeString(buf, song.header.Song_original_author);
            CommonNetwork.writeString(buf, song.header.Song_description);
        }
        buf.putShort(song.header.Song_tempo); //that one is important;
        if(sendExtraData){
            buf.put(song.header.Auto_saving);
            buf.put(song.header.Auto_saving_duration);
        }
        buf.put(song.header.Time_signature);
        if(sendExtraData){
            //There comes a lot of only editor relevant data...
            buf.putInt(song.header.Minutes_spent);
            buf.putInt(song.header.Left_clicks);
            buf.putInt(song.header.Right_clicks);
            buf.putInt(song.header.Note_blocks_added);
            buf.putInt(song.header.Note_blocks_removed);
            CommonNetwork.writeString(buf, song.header.MIDI_Schematic_file_name);
        }
        buf.put(song.header.Loop_on_off);
        buf.put(song.header.Max_loop_count);
        buf.putShort(song.header.Loop_start_tick);
        buf.putShort((short) song.getLayers().size());
        writeLayersAndNotes(buf);
    }
    public void writeLayersAndNotes(ByteBuffer buf){
        for(Layer layer:song.getLayers()){
            if(sendExtraData){
                CommonNetwork.writeString(buf, layer.name);
                buf.put(layer.lock);
            }
            buf.put(layer.volume);
            buf.put(layer.stereo);
            int tick = -1;
            for(Layer.Note note:layer.notes){
                buf.putShort((short) (note.tick - tick));
                tick = note.tick; //before I forget it
                buf.put(note.instrument);
                buf.put(note.key);
                buf.put(note.velocity);
                buf.put(note.panning);
                buf.putShort(note.pitch);
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
        sendExtraData = buf.get() != 0;
        NBS.Builder builder = new NBS.Builder();
        Header header = builder.header;
        header.Vanilla_instrument_count = buf.get();
        if(sendExtraData) {
            header.Song_length = buf.getShort();
            header.Song_name = CommonNetwork.readString(buf);
            header.Song_author = CommonNetwork.readString(buf);
            header.Song_original_author = CommonNetwork.readString(buf);
            header.Song_description = CommonNetwork.readString(buf);
        }
        header.Song_tempo = buf.getShort();
        if(sendExtraData){
            header.Auto_saving = buf.get();
            header.Auto_saving_duration = buf.get();
        }
        header.Time_signature = buf.get();
        if(sendExtraData){
            header.Minutes_spent = buf.getInt();
            header.Left_clicks = buf.getInt();
            header.Right_clicks = buf.getInt();
            header.Note_blocks_added = buf.getInt();
            header.Note_blocks_removed = buf.getInt();
            header.MIDI_Schematic_file_name = CommonNetwork.readString(buf);
        }
        header.Loop_on_off = buf.get();
        header.Max_loop_count = buf.get();
        header.Loop_start_tick = buf.getShort();

        header.Layer_count = buf.getShort();

        this.song = builder.build();

        readLayersAndNotes(buf);

        return valid;
    }

    void readLayersAndNotes(ByteBuffer buf) throws IOException {
        int length = 0;
        for(Layer layer:song.getLayers()){ //Layers are existing but not configured.
            boolean locked = false;
            if(sendExtraData){
                layer.name = CommonNetwork.readString(buf);
                locked = buf.get() != 0;
            }
            layer.volume = buf.get();
            layer.stereo = buf.get();

            int tick = -1;
            for(int step = buf.getShort(); step != 0; step = buf.getShort()){
                tick += step;
                Layer.Note note = layer.addNote(tick);
                if(note == null){
                    valid = false; //IDK what to do here
                    return;
                }
                note.instrument = buf.get();
                note.key = buf.get();
                note.velocity = buf.get();
                note.panning = buf.get();
                note.pitch = buf.getShort();
                length = Math.max(length, tick);
            }
            layer.setLock(locked); //If I lock it too early, I won't be able to add the notes to the layer...
        }
        this.song.setLength(length);
    }

}
