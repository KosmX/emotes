package com.kosmx.emotecraftCommon.opennbs.format;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class Layer {
    /**
     * The name of the layer.
     */
    public String name;
    /**
     * Whether or not this layer has been marked as locked. 1 = locked.
     */
    public byte lock;
    public boolean getLock(){
        return this.lock != 0;
    }
    public void setLock(boolean newValue){
        this.lock = (byte) (newValue ? 1 : 0);
    }
    /**
     * The volume of the layer (percentage). Ranges from 0-100.
     */
    public byte volume = 100;
    /**
     * How much this layer is panned to the left/right. 0 is 2 blocks right, 100 is center, 200 is 2 blocks left.
     */
    public byte stereo = 100;

    private int lastUsedTickPos = 0;

    public final ArrayList<Note> notes = new ArrayList<>();

    /**
     *
     * @param tick
     * @return
     */
    public int findAtTick(int tick){
        int i = - 1;
        if(this.notes.size() > lastUsedTickPos + 1 && this.notes.get(lastUsedTickPos + 1).tick <= tick) i = lastUsedTickPos;
        while(this.notes.size() > i + 1 && this.notes.get(i + 1).tick <= tick){
            i++;
        }
        lastUsedTickPos = i;
        return i;
    }

    @Nullable
    public Note addNote(int tick){
        if(this.getLock()){
            return null;
        }
        int i = findAtTick(tick);
        if(i > 0 && notes.get(i).tick == tick){
            return null;
        }
        Note note = new Note(tick);
        notes.add(i + 1, note);
        return note;
    }

    public ArrayList<Note> getNotesFrom(int fromTick, int toTick) {
        ArrayList<Note> returnNotes = new ArrayList<>();
        int posAtBegin = findAtTick(fromTick);
        if(notes.size() >= posAtBegin){
            posAtBegin++;
        }
        int posAtEnd = findAtTick(toTick);
        if(notes.size()>= posAtEnd){
            posAtEnd++;
        }
        if(posAtBegin < 0){
            posAtBegin = 0;
        }
        for(; posAtBegin < posAtEnd; posAtBegin++){
            returnNotes.add(this.notes.get(posAtBegin));
        }
        return returnNotes;
    }

    public ArrayList<Note> getNotesFrom(int toTick) {
        return getNotesFrom(this.lastUsedTickPos, toTick);
    }

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
        public byte instrument;
        /**
         * The key of the note block, from 0-87, where 0 is A0 and 87 is C8. 33-57 is within the 2-octave limit.
         */
        public byte key;
        /**
         * The velocity/volume of the note block, from 0% to 100%.
         */
        public byte velocity = 100;
        /**
         * The stereo position of the note block, from 0-200. 100 is center panning.
         */
        public byte panning = 100;
        /**
         * The fine pitch of the note block, from -32,768 to 32,767 cents (but the max in Note Block Studio is limited to -1200 and +1200). 0 is no fine-tuning. ±100 cents is a single semitone difference. After reading this, we go back to step 2.
         */
        public short pitch = 0;

        /**
         * Where is that note exactly. to be able to search without replaying the whole binary.
         */
        public final int tick;

        public Note(int tick) {
            this.tick = tick;
        }

        /**
         * @return sound's pitch as a float
         */
        public float getPitch(){
            return (float)Math.pow(2.0D, (double)(this.key + this.pitch/100 - 45) / 12.0D); //key 45 is F#
        }

        /**
         * @return sound value in percents (including the channels volume)
         */
        public float getVolume(){
            return this.velocity/10000f*volume; //there is why notes can't be static
        }
    }

}
