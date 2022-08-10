package io.github.kosmx.emotes.server.serializer.type;


import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.quarktool.QuarkParsingError;
import dev.kosmx.playerAnim.core.data.quarktool.QuarkReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QuarkReaderWrapper implements IReader {
    @Override
    public List<KeyframeAnimation> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        QuarkReader quarkReader = new QuarkReader();
        BufferedReader reader = streamReader(inputStream);
        try {
            quarkReader.deserialize(reader, filename);
            ArrayList<KeyframeAnimation> list = new ArrayList<>();
            list.add(quarkReader.getEmote());
            return list;
        } catch (QuarkParsingError quarkParsingError) {
            throw new EmoteSerializerException("Quark error", getFormatExtension(), quarkParsingError);
        }
    }

    @Override
    public AnimationFormat getFormatType() {
        return AnimationFormat.QUARK;
    }
}
