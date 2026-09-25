package pro.komaru.tridot.common;

import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.*;
import pro.komaru.tridot.api.Utils;

public class ServerTickHandler{

    public static int tick;

    public static void preInit(IEventBus gameBus){
        gameBus.addListener(EventPriority.NORMAL, false, ServerTickEvent.Post.class, ServerTickHandler::serverTick);
        gameBus.addListener(EventPriority.NORMAL, false, ServerStartingEvent.class, ServerTickHandler::serverStarting);
    }

    private static void serverTick(final ServerTickEvent.Post serverTickEvent){
        tick++;
        Utils.Schedule.handleSyncScheduledTasks(tick);
    }

    private static void serverStarting(final ServerStartingEvent serverStartingEvent){
        Utils.Schedule.serverStartupTasks();
    }
}
