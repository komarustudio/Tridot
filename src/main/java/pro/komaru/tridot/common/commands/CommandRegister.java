package pro.komaru.tridot.common.commands;

import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.event.*;
import pro.komaru.tridot.*;

@EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegister{

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e){
        ModCommand.register(e.getDispatcher());
    }
}
