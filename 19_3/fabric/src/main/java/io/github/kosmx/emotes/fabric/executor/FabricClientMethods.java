package io.github.kosmx.emotes.fabric.executor;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.executor.AbstractClientMethods;
import io.github.kosmx.emotes.fabric.network.ClientNetworkInstance;

public class FabricClientMethods extends AbstractClientMethods {
    @Override
    public INetworkInstance getServerNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }
}
