package io.github.kosmx.emotes.forge.executor;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.executor.AbstractClientMethods;
import io.github.kosmx.emotes.forge.network.ClientNetworkInstance;

@SuppressWarnings("unchecked")
public class ForgeClientMethods extends AbstractClientMethods {

    @Override
    public INetworkInstance getServerNetworkController() {
        return ClientNetworkInstance.networkInstance;
    }


}
