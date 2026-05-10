package pro.komaru.tridot.client.cinema;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CutsceneHelper{
    public static void init(Level level, AABB boundingBox, int ticks) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, boundingBox.inflate(32.0D))) {
            player.getPersistentData().putInt("TridotCinematicTicks", ticks);
            player.getPersistentData().putBoolean("TridotCinematic", true);
            stopAnger(player);
        }
    }

    public static void init(ServerPlayer player, int ticks) {
        player.getPersistentData().putBoolean("TridotCinematic", true);
        player.getPersistentData().putInt("TridotCinematicTicks", ticks);
        stopAnger(player);
    }

    public static void stop(ServerPlayer player) {
        player.getPersistentData().putBoolean("TridotCinematic", false);
    }

    public static void stopAnger(ServerPlayer player) {
        AABB bounds = player.getBoundingBox().inflate(64.0D);
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
}