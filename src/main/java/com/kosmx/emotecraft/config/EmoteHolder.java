package com.kosmx.emotecraft.config;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.Emote;

import java.util.ArrayList;
import java.util.List;

public class EmoteHolder {
    public final Emote emote;
    public final String name;
    public final String description;
    public final String author;
    public final int hash;
    public static List<EmoteHolder> list = new ArrayList<EmoteHolder>();

    /**
     *
     * @param emote {@link com.kosmx.emotecraft.Emote}
     * @param name Emote name
     * @param description Emote decription
     * @param author Name of the Author
     */
    EmoteHolder(Emote emote, String name, String description, String author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hash = hash;
    }



    public Emote getEmote(){
        return emote;
    }

    public static EmoteHolder deserializeJson(String json) throws JsonParseException {     //throws BowlingBall XD
        return EmoteSerializer.deserializer.fromJson(json, EmoteHolder.class);
    }
    public static void addEmoteToList(String json) throws JsonParseException{
        list.add(deserializeJson(json));
    }

}

