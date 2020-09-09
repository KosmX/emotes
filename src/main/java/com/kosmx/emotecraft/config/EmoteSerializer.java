package com.kosmx.emotecraft.config;

import com.google.gson.*;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import net.minecraft.text.*;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Type;


public class EmoteSerializer implements JsonDeserializer<EmoteHolder> {

    //Todo create error feedback about missing items (names)
    @Override
    public EmoteHolder deserialize(JsonElement p, Type typeOf, JsonDeserializationContext ctxt) throws JsonParseException {
        JsonObject node = p.getAsJsonObject();
        MutableText author = (MutableText) LiteralText.EMPTY;
        MutableText name = Text.Serializer.fromJson(node.get("name"));
        if(node.has("author")){
            author = Text.Serializer.fromJson(node.get("author"));
        }
        MutableText description = (MutableText) LiteralText.EMPTY;
        if(node.has("description")){
            description = (LiteralText) Text.Serializer.fromJson(node.get("description"));
        }
        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("author") || string.equals("comment") || string.equals("name") || string.equals("description") || string.equals("emote"))return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        Emote emote = emoteDeserializer(node.getAsJsonObject("emote"));
        return new EmoteHolder(emote, name, description, author, node.hashCode());
    }

    private Emote emoteDeserializer(JsonObject node) throws JsonParseException{
        int beginTick = 0;
        if(node.has("beginTick")){
            beginTick = node.get("beginTick").getAsInt();
        }
        int endTick = node.get("endTick").getAsInt();
        if(endTick <= 0)throw new JsonParseException("endTick must be bigger than 0");
        boolean isLoop = false;
        int returnTick = 0;
        if(node.has("isLoop") && node.has("returnTick")){
            isLoop = node.get("isLoop").getAsBoolean();
            returnTick = node.get("returnTick").getAsInt();
            if(isLoop && (returnTick >= endTick || returnTick < 0))throw new JsonParseException("return tick have to be smaller than endTick and not smaller than 0");
        }

        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("beginTick") || string.equals("comment") || string.equals("endTick") || string.equals("stopTick") || string.equals("degrees") || string.equals("moves") || string.equals("returnTick") || string.equals("isLoop"))return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        int resetTick = node.has("stopTick") ? node.get("stopTick").getAsInt() : endTick;
        boolean degrees = !node.has("degrees") || node.get("degrees").getAsBoolean();
        Emote emote = new Emote(beginTick, endTick, resetTick, isLoop, returnTick);
        moveDeserializer(emote, node.getAsJsonArray("moves"), degrees);
        return emote;
    }

    private void moveDeserializer(Emote emote, JsonArray node, boolean degrees){
        for (JsonElement n : node) {
            JsonObject obj = n.getAsJsonObject();
            int tick = obj.get("tick").getAsInt();
            String easing = obj.has("easing") ? obj.get("easing").getAsString() : "linear";
            obj.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("tick") || string.equals("comment") || string.equals("easing") || string.equals("turn") || string.equals("head") || string.equals("torso") || string.equals("rightArm") || string.equals("leftArm") || string.equals("rightLeg") || string.equals("leftLeg"))return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            int turn = obj.has("turn") ? obj.get("turn").getAsInt() : 0;
            addBodyPartIfExists(emote.head, "head", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.torso, "torso", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightArm, "rightArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftArm, "leftArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightLeg, "rightLeg", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftLeg, "leftLeg", obj, degrees, tick, easing, turn);
        }
    }
    private void addBodyPartIfExists(Emote.BodyPart part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            JsonObject partNode = node.get(name).getAsJsonObject();
            partNode.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("x") || string.equals("y") || string.equals("z") || string.equals("pitch") || string.equals("yaw") || string.equals("roll") || string.equals("comment") || string.equals("bend") || string.equals("axis"))return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            addPartIfExists(part.x, "x", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.y, "y", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.z, "z", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.pitch, "pitch", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.yaw, "yaw", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.roll, "roll", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.bend, "bend", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.axis, "axis", partNode, degrees, tick, easing, turn);
        }
    }
    private void addPartIfExists(Emote.Part part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            Emote.addMove(part, tick, node.get(name).getAsFloat(), easing, turn, degrees);
        }
    }
}
