package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.arch.ClientCommands;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CommonData.MOD_ID, dist = Dist.CLIENT)
public class EmotecraftClientNeoMod extends EmotecraftClientMod {

    public EmotecraftClientNeoMod(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, screen) -> new EmoteMenu(screen));
        super.onInitializeClient();

        NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onLoggingIn);
        modEventBus.addListener(this::onRegisterKeyMappings);
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        super.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientNetwork.INSTANCE.disconnect();
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientNetwork.INSTANCE.configureOnPlay(ClientPacketDistributor::sendToServer);
    }

    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU_KEY);
        event.register(STOP_EMOTE_KEY);
    }

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
