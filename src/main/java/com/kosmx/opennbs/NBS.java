package com.kosmx.opennbs;

import com.kosmx.opennbs.format.CustomInstrument;
import com.kosmx.opennbs.format.Header;
import com.kosmx.opennbs.format.Layer;

import java.util.ArrayList;
import java.util.List;

public class NBS {
    public final Header header;
    final ArrayList<Layer> layers;
    int length;
    byte customInstrumentCount;
    final ArrayList<CustomInstrument> customInstruments;



    public NBS(Header header, ArrayList<Layer> layers, ArrayList<CustomInstrument> customInstruments) {
        if(header.Layer_count != layers.size()){
            throw new IllegalArgumentException("Layer count have to be same in the header with the layers size");
        }
        this.header = header;
        this.layers = layers;
        this.customInstruments = customInstruments;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    List<Layer.Note> getNotesUntilTick(int tickFrom, int tickTo){
        ArrayList<Layer.Note> notes = new ArrayList<>();
        for(Layer layer:this.layers){
            if(tickFrom > tickTo){
                notes.addAll(layer.getNotesFrom(tickFrom, this.length));
                notes.addAll(layer.getNotesFrom(-1, tickTo));
            }
            else {
                notes.addAll(layer.getNotesFrom(tickFrom, tickTo));
            }
        }
        return notes;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = (length/(int) (header.Time_signature) + 1)*header.Time_signature;
    }

    public static class Builder{
        public Header header;
        public ArrayList<Layer> layers;
        public ArrayList<CustomInstrument> customInstruments;

        public NBS build(){
            return new NBS(header, layers, customInstruments);
        }
    }
}
