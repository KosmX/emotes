package io.github.kosmx.emotes.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EmoteFixer{
    private final int currentVersion;

    @Nullable
    private JsonElement data = null;

    public EmoteFixer(int version){
        currentVersion = version;
    }

    public UUID getEmoteID(JsonElement element) {
        try {
            int id = 0;
            UUID uuid = null;
            if (currentVersion < 4) {
                id = element.getAsInt();
            } else {
                uuid = UUID.fromString(element.getAsString());
            }
            for (int i = currentVersion; i < SerializableConfig.staticConfigVersion; i++) {
                if (getData().has(Integer.toString(i))) {
                    if (i < 3) {
                        if (getData().get(Integer.toString(i)).getAsJsonObject().has(String.valueOf(id))) {
                            id = getData().get(String.valueOf(i)).getAsJsonObject().get(String.valueOf(id)).getAsInt();
                        }
                    } else if (i == 3) { //It is true, now. But it won't be true forever
                        if (getData().get(Integer.toString(i)).getAsJsonObject().has(String.valueOf(id))) {
                            uuid = UUID.fromString(getData().get(String.valueOf(i)).getAsJsonObject().get(String.valueOf(id)).getAsString());
                        }
                    } else {
                        if (getData().get(Integer.toString(i)).getAsJsonObject().has(String.valueOf(uuid))) {
                            uuid = UUID.fromString(getData().get(String.valueOf(i)).getAsJsonObject().get(String.valueOf(uuid)).getAsString());
                        }
                    }
                }
            }
            return uuid;
        } catch(Exception e) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return new UUID(0, 0);
            }
            else {
                return UUID.fromString(element.getAsString());
            }
        }
    }

    private JsonObject getData(){
        if(data == null){
            try{
                InputStream stream = EmoteFixer.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emote_upgrade_data.json");
                InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);
                //data = ClientSerializer.serializer.fromJson(reader, new TypeToken<HashMap<Integer, HashMap<Integer, Integer>>>(){}.getType());
                data = new JsonParser().parse(reader);
            }catch (JsonParseException | NullPointerException e){
                e.printStackTrace();
            }
        }
        return data.getAsJsonObject();
    }

}
