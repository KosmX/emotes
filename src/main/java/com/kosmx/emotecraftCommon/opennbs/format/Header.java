package com.kosmx.emotecraftCommon.opennbs.format;

/**
 * Sound format header. contains Name, click amount, length, everything from header
 * field names and descriptions are form https://opennbs.org/nbs
 *
 * Deprecated fields are not used by OPEN NOTE BLOCK STUDIO
 */
public class Header {
    /**
     * The version of the new NBS format.
     */
    public byte NBS_version;
    /**
     * Amount of default instruments when the song was saved. This is needed to determine at what index custom instruments start.
     */
    public byte Vanilla_instrument_count;
    /**
     * The length of the song, measured in ticks. Divide this by the tempo to get the length of the song in seconds. Note Block Studio doesn't really care about this value, the song size is calculated in the second part.
     * (Note: this was re-added in NBS version 3)
     */
    @Deprecated
    public short Song_length;
    /**
     * The last layer with at least one note block in it, or the last layer that has had its name, volume or stereo changed.
     */
    public short Layer_count;
    /**
     * The name of the song.
     */
    public String Song_name;
    /**
     * The author of the song.
     */
    public String Song_author;
    /**
     * The original author of the song.
     */
    public String Song_original_author;
    /**
     * The description of the song.
     */
    public String Song_description;
    /**
     * The tempo of the song multiplied by 100 (for example, 1225 instead of 12.25). Measured in ticks per second.
     */
    public short Song_tempo;
    /**
     * Whether auto-saving has been enabled (0 or 1). As of NBS version 4 this value is still saved to the file, but no longer used in the program.
     */
    @Deprecated
    public byte Auto_saving;
    public boolean Auto_saving(){
        return this.Auto_saving != 0;
    }
    /**
     * The amount of minutes between each auto-save (if it has been enabled) (1-60). As of NBS version 4 this value is still saved to the file, but no longer used in the program.
     */
    @Deprecated
    public byte Auto_saving_duration;
    /**
     * The time signature of the song. If this is 3, then the signature is 3/4. Default is 4. This value ranges from 2-8.
     */
    public byte Time_signature;
    /**
     * Amount of minutes spent on the project.
     */
    public int Minutes_spent;
    /**
     * Amount of times the user has left-clicked.
     */
    public int Left_clicks;
    /**
     * Amount of times the user has right-clicked.
     */
    public int Right_clicks;
    /**
     * Amount of times the user has added a note block.
     */
    public int Note_blocks_added;
    /**
     * The amount of times the user have removed a note block.
     */
    public int Note_blocks_removed;
    /**
     * If the song has been imported from a .mid or .schematic file, that file name is stored here (only the name of the file, not the path).
     */
    public String MIDI_Schematic_file_name;
    /**
     * Whether looping is on or off. (0 = off, 1 = on)
     */
    public byte Loop_on_off;
    public boolean Loop_on_off(){
        return this.Loop_on_off != 0;
    }
    /**
     * 0 = infinite. Other values mean the amount of times the song loops.
     */
    public byte Max_loop_count;
    /**
     * Determines which part of the song (in ticks) it loops back to.
     */
    public short Loop_start_tick;

    //This is the end of the header

}
