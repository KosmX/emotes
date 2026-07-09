package io.github.kosmx.emotes;

import org.redlance.common.services.AdvancedService;
import org.redlance.common.services.ServiceUtils;

public interface EmotecraftModPlatform extends AdvancedService {
    EmotecraftModPlatform INSTANCE = ServiceUtils.loadService(EmotecraftModPlatform.class);

    String getModVersion(String modid);
    String getPlatformName();
}
