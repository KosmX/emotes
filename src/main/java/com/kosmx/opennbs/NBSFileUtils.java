package com.kosmx.opennbs;

import com.kosmx.opennbs.format.Header;
import com.kosmx.opennbs.format.Layer;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * R/W nbs files
 */
public class NBSFileUtils {

    //some methods are from EmotecraftCommon. these have to be separated if I'll make a lib from this!!!
    public static NBS read(DataInputStream stream) throws IOException {
        if(stream.readShort() != 0){
            throw new IOException("Can't read old NBS format.");
        }
        NBS.Builder songBuilder = new NBS.Builder();
        Header header = songBuilder.header;
        header.NBS_version = stream.readByte();
        int version = header.NBS_version; //just for faster coning
        header.Vanilla_instrument_count = stream.readByte();
        if(version >= 3)header.Song_length = stream.readShort();
        header.Layer_count = stream.readShort();
        header.Song_name = readString(stream);
        header.Song_author = readString(stream);
        header.Song_original_author = readString(stream);
        header.Song_description = readString(stream);
        header.Song_tempo = stream.readShort();
        header.Auto_saving = stream.readByte();
        header.Auto_saving_duration = stream.readByte();
        header.Time_signature = stream.readByte();
        header.Minutes_spent = stream.readInt();
        header.Left_clicks = stream.readInt();
        header.Right_clicks = stream.readInt();
        header.Note_blocks_added = stream.readInt();
        header.Note_blocks_removed = stream.readInt();
        header.MIDI_Schematic_file_name = readString(stream);
        if(version >= 4){   //looping
            header.Loop_on_off = stream.readByte();
            header.Max_loop_count = stream.readByte();
            header.Loop_start_tick = stream.readShort();
        }

        //Part 2

        for(int i = 0; i < header.Layer_count; i++){
            songBuilder.layers.add(new Layer()); //Precreate layers for later use :)
        }


        int tick = -1;
        for(short jumpToTheNextTick = stream.readShort(); jumpToTheNextTick != 0; jumpToTheNextTick = stream.readShort()){
            tick += jumpToTheNextTick;
            for(int layer = -1, jumpToTheNextLayer = stream.readShort(); jumpToTheNextLayer != 0; jumpToTheNextLayer = stream.readShort()){
                layer += jumpToTheNextLayer;
                byte instrument = stream.readByte();
                songBuilder.layers.get(layer)
            }
        }

    }

    static String readString(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        if(len < 0){
            throw new IOException("The string's length is less than zero. You wanna me to read it backwards???");
        }
        byte[] bytes = new byte[len];
        if(stream.read(bytes) != len){
            throw new IOException("Invalid string");
        }
        return new String(bytes, StandardCharsets.UTF_8); //:D
    }

}
