package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmotecraftMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(CommonData.MOD_ID)
public class EmotecraftNeoMod extends EmotecraftMod {
    public EmotecraftNeoMod(Dist dist) {
        super.onInitialize(dist.isClient());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ServerCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        super.onStartTracking(event.getTarget(), event.getEntity());
    }
}
