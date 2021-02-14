package com.kosmx.opennbs.format;

public class Layer {
    /**
     * The name of the layer.
     */
    public String Name;
    /**
     * Whether or not this layer has been marked as locked. 1 = locked.
     */
    public byte Lock;
    public boolean getLock(){
        return this.Lock != 0;
    }
    public void setLock(boolean newValue){
        this.Lock = (byte) (newValue ? 1 : 0);
    }

    public boolean addNote(byte instrument, byte key, byte velocity, byte panning, short pitch)

    public class Note {
        /**
         * The instrument of the note block. This is 0-15, or higher if the song uses custom instruments.
         * 0 = Piano (Air)
         * 1 = Double Bass (Wood)
         * 2 = Bass Drum (Stone)
         * 3 = Snare Drum (Sand)
         * 4 = Click (Glass)
         * 5 = Guitar (Wool)
         * 6 = Flute (Clay)
         * 7 = Bell (Block of Gold)
         * 8 = Chime (Packed Ice)
         * 9 = Xylophone (Bone Block)
         * 10 = Iron Xylophone (Iron Block)
         * 11 = Cow Bell (Soul Sand)
         * 12 = Didgeridoo (Pumpkin)
         * 13 = Bit (Block of Emerald)
         * 14 = Banjo (Hay)
         * 15 = Pling (Glowstone)
         */
        public byte Instrument;
        /**
         * The key of the note block, from 0-87, where 0 is A0 and 87 is C8. 33-57 is within the 2-octave limit.
         */
        public byte Key;
        /**
         * The velocity/volume of the note block, from 0% to 100%.
         */
        public byte Velocity = 100;
        /**
         * The stereo position of the note block, from 0-200. 100 is center panning.
         */
        public byte Panning = 100;
        /**
         * The fine pitch of the note block, from -32,768 to 32,767 cents (but the max in Note Block Studio is limited to -1200 and +1200). 0 is no fine-tuning. ±100 cents is a single semitone difference. After reading this, we go back to step 2.
         */
        public short Pitch = 0;

    }

}
