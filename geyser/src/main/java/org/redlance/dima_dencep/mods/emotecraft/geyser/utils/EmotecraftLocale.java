package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.text.GeyserLocale;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class EmotecraftLocale {
    private static final Map<String, Map<String, String>> LOCALE_MAPPINGS = new HashMap<>();

    static {
        loadLocale("en_us");
        loadLocale(GeyserLocale.getDefaultLocale().toLowerCase(Locale.ROOT));
    }

    public static void loadLocale(String locale) {
        if (LOCALE_MAPPINGS.containsKey(locale)) return;

        try (InputStream localeStream = EmotecraftLocale.class.getResourceAsStream("/assets/emotecraft/lang/" + locale + ".json")) {
            JsonNode localeObj = GeyserImpl.JSON_MAPPER.readTree(localeStream);
            Iterator<Map.Entry<String, JsonNode>> localeIterator = localeObj.fields();
            Map<String, String> langMap = new HashMap<>();

            while (localeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = localeIterator.next();
                langMap.put(entry.getKey(), entry.getValue().asText());
            }

            LOCALE_MAPPINGS.put(locale, langMap);
        } catch (FileNotFoundException e) {
            throw new AssertionError(GeyserLocale.getLocaleStringLog("geyser.locale.fail.file", locale, e.getMessage()));
        } catch (Exception e) {
            throw new AssertionError(GeyserLocale.getLocaleStringLog("geyser.locale.fail.json", locale), e);
        }
    }

    public static String getLocaleString(String messageText, String locale) {
        loadLocale(locale.toLowerCase(Locale.ROOT));

        Map<String, String> localeStrings = LOCALE_MAPPINGS.get(locale.toLowerCase(Locale.ROOT));
        if (localeStrings == null) {
            localeStrings = LOCALE_MAPPINGS.get(GeyserLocale.getDefaultLocale().toLowerCase(Locale.ROOT));

            if (localeStrings == null) {
                CommonData.LOGGER.warn("MISSING DEFAULT LOCALE: {}", GeyserLocale.getDefaultLocale());
                return messageText;
            }
        }
        return localeStrings.getOrDefault(messageText, messageText);
    }
}
