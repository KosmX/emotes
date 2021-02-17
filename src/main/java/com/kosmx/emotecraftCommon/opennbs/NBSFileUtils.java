package com.kosmx.emotecraftCommon.opennbs;

import com.kosmx.emotecraftCommon.opennbs.format.Header;
import com.kosmx.emotecraftCommon.opennbs.format.Layer;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * R/W nbs files
 */

public class NBSFileUtils {

    final static int maxWorkingVersion = 5;

    //some methods are from EmotecraftCommon. these have to be separated if I'll make a lib from this!!!
    public static NBS read(DataInputStream stream) throws IOException {
        if(readShort(stream) != 0){
            throw new IOException("Can't read old NBS format.");
        }
        NBS.Builder songBuilder = new NBS.Builder();
        Header header = songBuilder.header;
        header.NBS_version = stream.readByte();
        int version = header.NBS_version; //just for faster coning
        if(version > maxWorkingVersion) throw new IOException("Can't read newer NBS format than " + maxWorkingVersion + "."); //I'll probably run into this
        header.Vanilla_instrument_count = stream.readByte();
        if(version >= 3)header.Song_length = readShort(stream);
        header.Layer_count = readShort(stream);
        header.Song_name = readString(stream);
        header.Song_author = readString(stream);
        header.Song_original_author = readString(stream);
        header.Song_description = readString(stream);
        header.Song_tempo = readShort(stream);
        header.Auto_saving = stream.readByte();
        header.Auto_saving_duration = stream.readByte();
        header.Time_signature = stream.readByte();
        header.Minutes_spent = readInt(stream);
        header.Left_clicks = readInt(stream);
        header.Right_clicks = readInt(stream);
        header.Note_blocks_added = readInt(stream);
        header.Note_blocks_removed = readInt(stream);
        header.MIDI_Schematic_file_name = readString(stream);
        if(version >= 4){   //looping
            header.Loop_on_off = stream.readByte();
            header.Max_loop_count = stream.readByte();
            header.Loop_start_tick = readShort(stream);
        }

        //Part 2

        for(int i = 0; i < header.Layer_count; i++){
            songBuilder.layers.add(new Layer()); //Precreate layers for later use :)
        }

        int maxLength = 0;

        int tick = -1;
        for(short jumpToTheNextTick = readShort(stream); jumpToTheNextTick != 0; jumpToTheNextTick = readShort(stream)){
            tick += jumpToTheNextTick;
            for(int layer = -1, jumpToTheNextLayer = readShort(stream); jumpToTheNextLayer != 0; jumpToTheNextLayer = readShort(stream)){
                layer += jumpToTheNextLayer;
                Layer.Note note = songBuilder.layers.get(layer).addNote(tick);
                if(note == null){
                    throw new IOException("Creeper, Aww man"); //sry for putting this into an MC song stuff
                }
                note.instrument = stream.readByte();
                note.key = stream.readByte();
                if(version >= 4){
                    note.velocity = stream.readByte();
                    note.panning = stream.readByte();
                    note.pitch = readShort(stream);
                }
                maxLength = Math.max(maxLength, tick);
            }
        }
        //Part 3 :

        for (Layer layer: songBuilder.layers){
            layer.name = readString(stream);
            layer.lock = stream.readByte();
            layer.volume = stream.readByte();
            layer.stereo = stream.readByte();
        }
        if(stream.readByte() != 0){
            throw new IOException("NBSUtils can not handle custom instruments (yet)");
        }

        NBS song = songBuilder.build();
        song.setLength(maxLength);
        return song;
    }

    static String readString(DataInputStream stream) throws IOException {
        int len = readInt(stream);
        if(len < 0){
            throw new IOException("The string's length is less than zero. You wanna me to read it backwards???");
        }
        byte[] bytes = new byte[len];
        if(stream.read(bytes) != len){
            throw new IOException("Invalid string");
        }
        return new String(bytes, StandardCharsets.UTF_8); //:D
    }

    static int readInt(DataInputStream stream) throws IOException{
        int i = 0;
        for(int n = 0; n<4; n++){
            i |= (stream.read() << (8*n));
        }
        return i;
    }
    static short readShort(DataInputStream stream) throws IOException{
        short i = 0;
        for(int n = 0; n<2; n++){
            i |= (stream.read() << (8*n));
        }
        return i;
    }

    //public void write //TODO
    //Emotecraft does not need to write these. so maybe later

}
