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


    public static class Builder{
        public Header header;
        public ArrayList<Layer> layers;
        public ArrayList<CustomInstrument> customInstruments;

        public NBS build(){
            return new NBS(header, layers, customInstruments);
        }
    }
}
