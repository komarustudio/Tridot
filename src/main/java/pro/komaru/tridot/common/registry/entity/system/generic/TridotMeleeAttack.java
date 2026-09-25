package pro.komaru.tridot.common.registry.entity.system.generic;

import net.minecraft.resources.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.common.registry.entity.*;
import pro.komaru.tridot.common.registry.entity.system.*;

public class TridotMeleeAttack extends AttackInstance{
    public final float speedModifier;
    public int ticksUntilNextPathRecalc;
    public double lastTargetX, lastTargetY, lastTargetZ;

    public TridotMeleeAttack(PathfinderMob mob, float speedMod, float range, int attackDelay, int attackDuration, int cooldown){
        super(mob, 0, range, attackDelay, attackDuration, cooldown);
        this.speedModifier = speedMod;
    }

    @Override
    public ResourceLocation getId(){
        return Tridot.ofTridot("melee");
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--ticksUntilNextPathRecalc <= 0 && (distanceToStoredTargetSqr(target) >= 1.0D || mob.getRandom().nextFloat() < 0.05F)) {
            storeTargetPosition();
            this.ticksUntilNextPathRecalc += 5;
            mob.getNavigation().moveTo(target, speedModifier);
        }

        double distSq = this.mob.distanceToSqr(target);
        if(distSq > 1024.0D){
            this.ticksUntilNextPathRecalc += 10;
        }else if(distSq > 256.0D){
            this.ticksUntilNextPathRecalc += 5;
        }
    }

    public double distanceToStoredTargetSqr(LivingEntity target) {
        return target.distanceToSqr(lastTargetX, lastTargetY, lastTargetZ);
    }

    public void storeTargetPosition() {
        LivingEntity target = mob.getTarget();
        this.lastTargetX = target.getX();
        this.lastTargetY = target.getY();
        this.lastTargetZ = target.getZ();
    }

    @Override
    public void start(AttackSystemMob systemMob){
        super.start(systemMob);
        mob.setAggressive(true);
        mob.level().broadcastEntityEvent(mob, (byte)4);

        this.ticksUntilNextPathRecalc = 0;
        this.mob.getNavigation().moveTo(mob.getTarget(), speedModifier);
        storeTargetPosition();
    }

    @Override
    public int preference(Entity entity){
        return 0;
    }

    @Override
    public void performAttack(){
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive()) {
            mob.swing(InteractionHand.MAIN_HAND);
            mob.doHurtTarget(target);
        }
    }
}
