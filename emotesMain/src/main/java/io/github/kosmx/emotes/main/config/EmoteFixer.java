package io.github.kosmx.emotes.main.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class EmoteFixer{
    private final int currentVersion;

    @Nullable
    private HashMap<Integer, HashMap<Integer, Integer>> data;

    public EmoteFixer(int version){
        currentVersion = version;
    }

    public int getEmoteID(JsonElement element){
        int id = element.getAsInt();
        if(currentVersion < SerializableConfig.staticConfigVersion) {
            for (int i = currentVersion; i < SerializableConfig.staticConfigVersion; i++) {
                if (getData().containsKey(i)){
                    if(getData().get(i).containsKey(id)){
                        id = getData().get(i).get(id);
                    }
                }
            }
        }
        return id;
    }

    private HashMap<Integer, HashMap<Integer, Integer>> getData(){
        if(data == null){
            try{
                InputStream stream = EmoteFixer.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "emoteUpgradeMap.json");
                InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);
                data = Serializer.serializer.fromJson(reader, new TypeToken<HashMap<Integer, HashMap<Integer, Integer>>>(){}.getType());
            }catch (JsonParseException e){
                e.printStackTrace();
                if(data == null){
                    data = new HashMap<>();
                }
            }
        }
        return data;
    }

}
