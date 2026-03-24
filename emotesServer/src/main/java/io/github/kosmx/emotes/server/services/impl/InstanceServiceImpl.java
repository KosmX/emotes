package io.github.kosmx.emotes.server.services.impl;

import io.github.kosmx.emotes.server.services.InstanceService;
import org.redlance.common.services.ServiceUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class InstanceServiceImpl implements InstanceService {
    @Override
    public Path getGameDirectory() {
        return Paths.get("");
    }

    @Override
    public int getPriority() {
        return ServiceUtils.LOWEST_PRIORITY;
    }

    @Override
    public boolean isServiceActive() {
        return true;
    }
}
