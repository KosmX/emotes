package io.github.kosmx.emotes.server.serializer.type;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.quarktool.QuarkReader;
import io.github.kosmx.emotes.server.config.Serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class QuarkReaderWrapper implements IReader {
    @Override
    public List<KeyframeAnimation> read(InputStream stream, String filename) throws EmoteSerializerException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            QuarkReader quarkReader = new QuarkReader();
            quarkReader.deserialize(reader, filename);

            return Collections.singletonList(quarkReader.getEmote());
        } catch (Throwable th) {
            throw new EmoteSerializerException("Quark error", getExtension(), th);
        }
    }

    @Override
    public String getExtension() {
        return "emote";
    }

    @Override
    public boolean isActive() {
        return Serializer.getConfig().enableQuark.get();
    }
}
