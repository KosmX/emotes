package com.kosmx.emotecraftCommon.opennbs.network;

import com.kosmx.emotecraftCommon.network.CommonNetwork;
import com.kosmx.emotecraftCommon.opennbs.NBS;
import com.kosmx.emotecraftCommon.opennbs.format.Header;
import com.kosmx.emotecraftCommon.opennbs.format.Layer;
import io.netty.buffer.ByteBuf;

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

    public void write(ByteBuf buf){
        buf.writeInt(packetVersion); //reserved for later use/changes
        buf.writeBoolean(sendExtraData);
        buf.writeByte(song.header.Vanilla_instrument_count);
        if(sendExtraData){
            buf.writeShort(song.header.Song_length);
            CommonNetwork.writeString(buf, song.header.Song_name);
            CommonNetwork.writeString(buf, song.header.Song_author);
            CommonNetwork.writeString(buf, song.header.Song_original_author);
            CommonNetwork.writeString(buf, song.header.Song_description);
        }
        buf.writeShort(song.header.Song_tempo); //that one is important;
        if(sendExtraData){
            buf.writeBoolean(song.header.Auto_saving());
            buf.writeByte(song.header.Auto_saving_duration);
        }
        buf.writeByte(song.header.Time_signature);
        if(sendExtraData){
            //There comes a lot of only editor relevant data...
            buf.writeInt(song.header.Minutes_spent);
            buf.writeInt(song.header.Left_clicks);
            buf.writeInt(song.header.Right_clicks);
            buf.writeInt(song.header.Note_blocks_added);
            buf.writeInt(song.header.Note_blocks_removed);
            CommonNetwork.writeString(buf, song.header.MIDI_Schematic_file_name);
        }
        buf.writeBoolean(song.header.Loop_on_off());
        buf.writeByte(song.header.Max_loop_count);
        buf.writeShort(song.header.Loop_start_tick);
        buf.writeShort(song.getLayers().size());
        writeLayersAndNotes(buf);
    }
    public void writeLayersAndNotes(ByteBuf buf){
        for(Layer layer:song.getLayers()){
            if(sendExtraData){
                CommonNetwork.writeString(buf, layer.name);
                buf.writeBoolean(layer.getLock());
            }
            buf.writeByte(layer.volume);
            buf.writeByte(layer.stereo);
            int tick = -1;
            for(Layer.Note note:layer.notes){
                buf.writeShort(note.tick - tick);
                tick = note.tick; //before I forget it
                buf.writeByte(note.instrument);
                buf.writeByte(note.key);
                buf.writeByte(note.velocity);
                buf.writeByte(note.panning);
                buf.writeShort(note.pitch);
            }
            buf.writeShort(0);//end of the notes
        }
    }

    /**
     *
     * @param buf input ByteBuf
     * @return true if reading was success
     */
    public boolean read(ByteBuf buf){
        version = buf.readInt();
        sendExtraData = buf.readBoolean();
        NBS.Builder builder = new NBS.Builder();
        Header header = builder.header;
        header.Vanilla_instrument_count = buf.readByte();
        if(sendExtraData) {
            header.Song_length = buf.readShort();
            header.Song_name = CommonNetwork.readString(buf);
            header.Song_author = CommonNetwork.readString(buf);
            header.Song_original_author = CommonNetwork.readString(buf);
            header.Song_description = CommonNetwork.readString(buf);
        }
        header.Song_tempo = buf.readShort();
        if(sendExtraData){
            header.Auto_saving = (byte) (buf.readBoolean() ? 1 : 0);
            header.Auto_saving_duration = buf.readByte();
        }
        header.Time_signature = buf.readByte();
        if(sendExtraData){
            header.Minutes_spent = buf.readInt();
            header.Left_clicks = buf.readInt();
            header.Right_clicks = buf.readInt();
            header.Note_blocks_added = buf.readInt();
            header.Note_blocks_removed = buf.readInt();
            header.MIDI_Schematic_file_name = CommonNetwork.readString(buf);
        }
        header.Loop_on_off = (byte) (buf.readBoolean() ? 1 : 0);
        header.Max_loop_count = buf.readByte();
        header.Loop_start_tick = buf.readShort();

        header.Layer_count = buf.readShort();

        this.song = builder.build();

        readLayersAndNotes(buf);

        return valid;
    }

    void readLayersAndNotes(ByteBuf buf){
        int length = 0;
        for(Layer layer:song.getLayers()){ //Layers are existing but not configured.
            boolean locked = false;
            if(sendExtraData){
                layer.name = CommonNetwork.readString(buf);
                locked = buf.readBoolean();
            }
            layer.volume = buf.readByte();
            layer.stereo = buf.readByte();

            int tick = -1;
            for(int step = buf.readShort(); step != 0; step = buf.readShort()){
                tick += step;
                Layer.Note note = layer.addNote(tick);
                if(note == null){
                    valid = false; //IDK what to do here
                    return;
                }
                note.instrument = buf.readByte();
                note.key = buf.readByte();
                note.velocity = buf.readByte();
                note.panning = buf.readByte();
                note.pitch = buf.readShort();
                length = Math.max(length, tick);
            }
            layer.setLock(locked); //If I lock it too early, I won't be able to add the notes to the layer...
        }
        this.song.setLength(length);
    }

}
