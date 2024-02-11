package io.github.kosmx.emotes.neoforge;

import io.github.kosmx.emotes.arch.ServerCommands;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.neoforge.executor.ForgeEmotesMain;
import io.github.kosmx.emotes.main.MainLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

@Mod("emotecraft")
public class ForgeWrapper {

    public static final Logger logger = LoggerFactory.getLogger(CommonData.MOD_ID);


    public ForgeWrapper(IEventBus modEventBus){
        EmoteInstance.instance = new ForgeEmotesMain();

        modEventBus.addListener(this::setup);

        NeoForge.EVENT_BUS.register(this);
        if(FMLLoader.getDist() == Dist.CLIENT){
            ClientInit.initClient(modEventBus);
        }
    }


    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ServerCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void clientCommandRegister(RegisterClientCommandsEvent event) {
        ClientCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void setup(final FMLCommonSetupEvent event){
        MainLoader.main(new String[]{"FML"});
        if(FMLLoader.getDist() == Dist.CLIENT){
            ClientInit.setupClient();
        }

    }

    public static void log(Level level, String msg){
        if (level.intValue() <= Level.INFO.intValue()) {
            logger.debug(msg);
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            logger.warn(msg);
        } else {
            logger.error(msg);
        }
    }

    public static void log(Level level, String msg, Throwable t){
        if (level.intValue() <= Level.INFO.intValue()) {
            logger.debug(msg, t);
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            logger.warn(msg, t);
        } else {
            logger.error(msg, t);
        }
    }
}
