package pro.komaru.tridot.client.cinema;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CutsceneHelper{
    private static final Map<UUID, Integer> activeCutscenes = new ConcurrentHashMap<>();
    private static final double DISTANCE = 32;

    public static void init(Level level, AABB boundingBox, int ticks) {
        if (ticks > 0) {
            for (Player player : level.getEntitiesOfClass(Player.class, boundingBox.inflate(DISTANCE))) {
                activeCutscenes.put(player.getUUID(), ticks);
                stopAnger(player);
            }
        }
    }

    public static void init(Player player, int ticks) {
        if (ticks > 0) {
            activeCutscenes.put(player.getUUID(), ticks);
            stopAnger(player);
        }
    }

    public static void stop(Player player) {
        activeCutscenes.remove(player.getUUID());
    }

    public static boolean isInCutscene(Player player) {
        return activeCutscenes.containsKey(player.getUUID());
    }

    public static void stopAnger(Player player) {
        AABB bounds = player.getBoundingBox().inflate(DISTANCE);
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, bounds);
        for (Mob mob : mobs) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
                var brain = mob.getBrain();
                if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                    brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event){
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (!player.level().isClientSide() && player instanceof ServerPlayer) {
            CutsceneHelper.activeCutscenes.computeIfPresent(player.getUUID(), (uuid, ticks) -> ticks > 1 ? ticks - 1 : null);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CutsceneHelper.stop(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event){
        if(event.getEntity() instanceof Player player && CutsceneHelper.isInCutscene(player)){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMobTarget(LivingChangeTargetEvent event){
        if(event.getNewTarget() instanceof Player player && CutsceneHelper.isInCutscene(player)){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event){
        var entity = event.getEntity();
        if(entity instanceof Player player && CutsceneHelper.isInCutscene(player)){
            event.setCanceled(true);
        }
    }
}