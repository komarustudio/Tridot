package pro.komaru.tridot.common.registry.entity.system;

import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;

import java.nio.file.*;

public abstract class AttackInstance{
    public final int attackDelay;
    public final int attackDuration;
    public final int cooldown;
    public float damage;
    public float range;

    public final PathfinderMob mob;
    public int cooldownTimer;
    public boolean isFinished = false;

    public AttackInstance(PathfinderMob mob, float damage, float range, int attackDelay, int attackDuration, int cooldown){
        this.mob = mob;
        this.damage = damage;
        this.range = range;
        this.attackDelay = attackDelay;
        this.attackDuration = attackDuration;
        this.cooldown = cooldown;
    }

    public abstract ResourceLocation getId();

    public boolean canUse(LivingEntity target){
        if (this.isOnCooldown()) {
            return false;
        }

        return isWithinAttackRange(target, range);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public abstract int preference(Entity target);

    public SoundEvent getPrepareSound() {
        return null;
    }

    public SoundEvent getAttackSound(){
        return null;
    }

    public void start(AttackSystemMob systemMob) {

    }

    public void stop() {

    }

    public void tick() {

    }

    public void setAttackOnCooldown() {
        cooldownTimer = cooldown;
    }

    public int getCooldownTimer(){
        return cooldownTimer;
    }

    public double getAttackRangeSqr(LivingEntity pEntity, double range) {
        return (mob.getBbWidth() * range * mob.getBbWidth() * range + pEntity.getBbWidth());
    }

    public double getPerceivedTargetDistanceSquareForAttack(LivingEntity target) {
        return mob.distanceToSqr(target.position());
    }

    public boolean isWithinAttackRange(LivingEntity pEntity, double range) {
        double d0 = this.getPerceivedTargetDistanceSquareForAttack(pEntity);
        return d0 <= this.getAttackRangeSqr(pEntity, range);
    }

    public boolean canPerformAttack(LivingEntity target) {
        return isWithinAttackRange(target, range);
    }

    public abstract void performAttack();

    public boolean isOnCooldown() {
        return cooldownTimer > 0;
    }
}
