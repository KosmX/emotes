package io.github.kosmx.emotes.server.geyser;

import io.github.kosmx.emotes.common.tools.BiMap;

import javax.annotation.Nullable;
import java.util.UUID;

public class EmoteMappings {
    //Bedrock - Java
    final BiMap<UUID, UUID> map;

    public EmoteMappings(BiMap<UUID, UUID> map){
        this.map = map;
    }

    @Nullable
    public UUID getBeEmote(UUID javaEmote){
        return map.getL(javaEmote);
    }

    @Nullable
    public UUID getJavaEmote(UUID beEmote){
        return map.getR(beEmote);
    }
}
