package pro.komaru.tridot.client;

import net.minecraft.client.*;
import net.neoforged.neoforge.client.event.*;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.common.ServerTickHandler;

public class ClientTick {

    public static int ticksInGame = 0;
    public static float partialTicks = 0;

    public static float getTotal(){
        return (float)ticksInGame + partialTicks;
    }

    public static float mcPartialTick(){
        Minecraft mc = Minecraft.getInstance();
        return mc.getTimer().getGameTimeDeltaPartialTick(!mc.isPaused());
    }

    public static void renderTick(RenderFrameEvent.Post event){
        partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(!Minecraft.getInstance().isPaused());
    }

    public static void clientTickEnd(ClientTickEvent.Post event){
        if(!Minecraft.getInstance().isPaused()){
            ticksInGame++;
            partialTicks = 0;
        }

        if(!Minecraft.getInstance().hasSingleplayerServer()){
            ServerTickHandler.tick++;
            Utils.Schedule.handleSyncScheduledTasks(ServerTickHandler.tick);
        }
    }

    public float time() {
        return ticksInGame + partialTicks;
    }
}
