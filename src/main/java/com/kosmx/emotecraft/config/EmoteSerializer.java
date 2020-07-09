package com.kosmx.emotecraft.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kosmx.emotecraft.Emote;

import java.io.IOException;
import java.util.Iterator;

public class EmoteSerializer extends StdDeserializer<EmoteHolder> {

    protected EmoteSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public EmoteHolder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String author = null;
        String name = node.get("name").asText();
        if(node.has("author")){
            author = node.get("author").asText();
        }
        String description = null;
        if(node.has("description")){
            description = node.get("description").asText();
        }
        Emote emote = emoteDeserializer(node.get("emote"));
        return new EmoteHolder(emote, name, description, author);
    }

    private Emote emoteDeserializer(JsonNode node){
        int beginTick = 0;
        if(node.has("beginTick")){
            beginTick = node.get("beginTick").asInt();
        }
        int endTick = node.get("endTick").asInt();
        int resetTick = node.has("resetTick") ? node.get("resetTick").asInt() : endTick;
        boolean degrees = !node.has("degrees") || node.get("degrees").asBoolean();
        Emote emote = new Emote(beginTick, endTick, resetTick);
        moveDeserializer(emote, node.get("moves"), degrees);
        return emote;
    }

    private void moveDeserializer(Emote emote, JsonNode node, boolean degrees){
        for (JsonNode n : node) {
            int tick = n.get("tick").asInt();
            String easing = n.has("easing") ? n.get("easing").asText() : "linear";
            int turn = n.has("turn") ? n.get("turn").asInt() : 0;
            addBodyPartIfExists(emote, emote.head, "head", n, degrees, tick, easing);
            addBodyPartIfExists(emote, emote.torso, "torso", n, degrees, tick, easing);
            addBodyPartIfExists(emote, emote.rightArm, "rightArm", n, degrees, tick, easing);
            addBodyPartIfExists(emote, emote.leftArm, "leftArm", n, degrees, tick, easing);
            addBodyPartIfExists(emote, emote.rightLeg, "rightLeg", n, degrees, tick, easing);
            addBodyPartIfExists(emote, emote.leftLeg, "leftLeg", n, degrees, tick, easing);
        }
    }
    private void addBodyPartIfExists(Emote emote, Emote.BodyPart part, String name, JsonNode node, boolean degrees, int tick, String easing){
        if(node.has(name)){
            JsonNode partNode = node.get(name);
            addPartIfExists(emote, part.x, "x", partNode, degrees, tick, easing);
            addPartIfExists(emote, part.y, "y", partNode, degrees, tick, easing);
            addPartIfExists(emote, part.z, "z", partNode, degrees, tick, easing);
            addPartIfExists(emote, part.pitch, "pitch", partNode, degrees, tick, easing);
            addPartIfExists(emote, part.yaw, "yaw", partNode, degrees, tick, easing);
            addPartIfExists(emote, part.roll, "roll", partNode, degrees, tick, easing);
        }
    }
    private void addPartIfExists(Emote emote, Emote.Part part, String name, JsonNode node, boolean degrees, int tick, String easing){
        if(node.has(name)){
            float value = (float) node.get(name).asDouble();
            if (degrees) value *= 0.01745329251f;
            emote.addMove(part, tick, value , easing);
        }
    }
}
