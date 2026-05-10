package pro.komaru.tridot.client.cinema;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CutsceneEvents{

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if(!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer){
            int cinematicTimer = player.getPersistentData().getInt("TridotCinematicTicks");
            if(cinematicTimer > 0){
                player.getPersistentData().putInt("TridotCinematicTicks", cinematicTimer - 1);
                if(cinematicTimer == 1){
                    player.getPersistentData().putBoolean("TridotCinematic", false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event){
        if(event.getEntity() instanceof Player player && player.getPersistentData().getBoolean("TridotCinematic")){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMobTarget(LivingChangeTargetEvent event){
        if(event.getNewTarget() instanceof Player player && player.getPersistentData().getBoolean("TridotCinematic")){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event){
        var entity = event.getEntity();
        if(entity instanceof Player player && player.getPersistentData().getBoolean("TridotCinematic")){
            event.setCanceled(true);
        }
    }
}