package io.github.kosmx.emotes.server.serializer.type;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.server.config.Serializer;

import java.io.InputStream;
import java.util.List;

public class QuarkReaderWrapper implements IReader {
    @Override
    public List<Animation> read(InputStream stream, String filename) throws EmoteSerializerException {
        /*try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            QuarkReader quarkReader = new QuarkReader();
            quarkReader.deserialize(reader, filename);

            return Collections.singletonList(quarkReader.getEmote());
        } catch (Throwable th) {
            throw new EmoteSerializerException("Quark error", getExtension(), th);
        }*/
        throw new EmoteSerializerException("Quark error", getExtension(), new UnsupportedOperationException("quark is not supported"));
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
