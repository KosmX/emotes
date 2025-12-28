package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.text.GeyserLocale;
import org.geysermc.geyser.util.JsonUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EmotecraftLocale {
    private static final Map<String, Map<String, String>> LOCALE_MAPPINGS = new HashMap<>();

    static {
        loadLocale("en_us");
        loadLocale(GeyserLocale.getDefaultLocale().toLowerCase(Locale.ROOT));
    }

    public static void loadLocale(String locale) {
        if (LOCALE_MAPPINGS.containsKey(locale)) return;

        try (InputStream localeStream = EmotecraftLocale.class.getResourceAsStream("/assets/emotecraft/lang/" + locale + ".json")) {
            JsonObject localeObj = JsonUtils.fromJson(Objects.requireNonNull(localeStream));
            Map<String, String> langMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : localeObj.entrySet()) {
                langMap.put(entry.getKey(), entry.getValue().getAsString());
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
