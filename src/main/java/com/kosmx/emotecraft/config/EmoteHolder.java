package com.kosmx.emotecraft.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kosmx.emotecraft.Emote;

@JsonDeserialize(using = EmoteSerializer.class)
public class EmoteHolder {
    public final Emote emote;
    public final String name;
    public final String description;
    public final String author;
    public final int hash;

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

    public static EmoteHolder deserializeJson(String json) throws JsonProcessingException {     //throws BowlingBall XD
        return new ObjectMapper().readValue(json, EmoteHolder.class);
    }

}
