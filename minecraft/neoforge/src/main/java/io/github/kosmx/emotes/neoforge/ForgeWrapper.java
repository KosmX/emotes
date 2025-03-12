package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.arch.ClientCommands;
import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.MainLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(CommonData.MOD_ID)
public class ForgeWrapper {
    public ForgeWrapper(Dist dist) {
        MainLoader.main(dist.isClient());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ServerCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void clientCommandRegister(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
