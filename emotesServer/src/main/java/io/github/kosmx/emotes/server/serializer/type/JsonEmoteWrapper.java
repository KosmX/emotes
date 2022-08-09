package io.github.kosmx.emotes.server.serializer.type;

import com.google.gson.JsonParseException;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonEmoteWrapper implements ISerializer {

    @Override
    public List<KeyframeAnimation> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        try{
            return AnimationSerializing.deserializeAnimation(inputStream);
        }catch (JsonParseException | IOException e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }

    @Override
    public void write(KeyframeAnimation emote, OutputStream outputStream) throws EmoteSerializerException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            AnimationSerializing.writeAnimation(emote, bufferedWriter);

        }catch (Exception e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }

    @Override
    public String getFormatExtension() {
        return "json";
    }

    @Override
    public AnimationFormat getFormatType() {
        return AnimationFormat.JSON_EMOTECRAFT;
    }
}
