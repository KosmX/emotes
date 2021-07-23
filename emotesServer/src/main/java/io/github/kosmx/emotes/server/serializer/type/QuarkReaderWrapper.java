package io.github.kosmx.emotes.server.serializer.type;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.quarktool.QuarkParsingError;
import io.github.kosmx.emotes.common.quarktool.QuarkReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QuarkReaderWrapper implements IReader {
    @Override
    public List<EmoteData> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        QuarkReader quarkReader = new QuarkReader();
        BufferedReader reader = streamReader(inputStream);
        try {
            quarkReader.deserialize(reader, filename);
            ArrayList<EmoteData> list = new ArrayList();
            list.add(quarkReader.getEmote());
            return list;
        } catch (QuarkParsingError quarkParsingError) {
            throw new EmoteSerializerException("Quark error", getFormatExtension(), quarkParsingError);
        }
    }

    @Override
    public String getFormatExtension() {
        return "emote";
    }
}
