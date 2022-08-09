package io.github.kosmx.emotes.server.serializer.type;


import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public interface IReader {
    List<KeyframeAnimation> read(InputStream reader, String filename) throws EmoteSerializerException;

    default String getFormatExtension(){
        return getFormatType().getExtension();
    }

    AnimationFormat getFormatType();

    default BufferedReader streamReader(InputStream stream){
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
