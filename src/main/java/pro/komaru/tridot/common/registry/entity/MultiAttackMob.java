package pro.komaru.tridot.common.registry.entity;

import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.api.entity.*;
import pro.komaru.tridot.util.*;

import javax.annotation.*;
import java.util.*;

public abstract class MultiAttackMob extends PathfinderMob{
    private static final EntityDataAccessor<String> DATA_ID = SynchedEntityData.defineId(MultiAttackMob.class, EntityDataSerializers.STRING);
    protected int preparingTickCount;
    protected int globalCooldown;
    protected int attackWarmupDelay;
    protected int attackAnimationTick;
    public AttackRegistry currentAttack = AttackRegistry.NONE;

    public MultiAttackMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel){
        super(pEntityType, pLevel);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(DATA_ID, AttackRegistry.NONE.toString());
    }

    public void readAdditionalSaveData(CompoundTag pCompound){
        super.readAdditionalSaveData(pCompound);
        this.preparingTickCount = pCompound.getInt("PrepareTicks");
    }

    public void addAdditionalSaveData(CompoundTag pCompound){
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("PrepareTicks", this.preparingTickCount);
    }

    public boolean isPreparingAttack(){
        if(this.level().isClientSide){
            return !this.entityData.get(DATA_ID).equals("none");
        }else{
            return this.preparingTickCount > 0;
        }
    }

    public boolean hasTarget(){
        return MultiAttackMob.this.getTarget() != null;
    }

    public int getPreparingTime(){
        return this.preparingTickCount;
    }

    public AttackRegistry getCurrentAttack(){
        return !this.level().isClientSide ? this.currentAttack : AttackRegistry.byId(this.entityData.get(DATA_ID));
    }

    @Override
    public void tick(){
        super.tick();
        if (attackAnimationTick > 0) {
            attackAnimationTick--;
            if (attackAnimationTick == attackDelay() && this.getTarget() != null && getTarget().isAlive()) {
                if (isWithinMeleeAttackRange(this.getTarget())){
                    performMeleeAttack();
                }
            }
        }
    }

    public void performMeleeAttack() {
        swing(InteractionHand.MAIN_HAND);
        doHurtTarget(getTarget());
        playSound(attackSound(), 1.0F, 1.0F);
    }

    /**
     * Animation delay
     */
    public int attackDelay() {
        return 5;
    }

    public SoundEvent attackSound() {
        return SoundEvents.PLAYER_ATTACK_STRONG;
    }

    protected void customServerAiStep(){
        super.customServerAiStep();
        if(this.globalCooldown > 0){
            --this.globalCooldown;
        }

        if(this.attackWarmupDelay > 0){
            --this.attackWarmupDelay;
        }

        if(this.preparingTickCount > 0){
            --this.preparingTickCount;
        }
    }

    public void setCurrentAttack(AttackRegistry pCurrentAttack){
        this.currentAttack = pCurrentAttack;
        this.entityData.set(DATA_ID, pCurrentAttack.getId());
    }

    public boolean isFleeing(Mob mob, float dist){
        return mob.getNavigation().getPath() != null && mob.getNavigation().getPath().getDistToTarget() > dist;
    }

    public boolean cantReachTarget(LivingEntity target){
        Path path = navigation.createPath(target, 1);
        return path == null;
    }

    public class PrepareGoal extends Goal{
        public PrepareGoal(){
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse(){
            return getPreparingTime() > 0;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start(){
            super.start();
            MultiAttackMob.this.navigation.stop();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop(){
            super.stop();
            MultiAttackMob.this.setCurrentAttack(AttackRegistry.NONE);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick(){
            if(MultiAttackMob.this.getTarget() != null){
                MultiAttackMob.this.getLookControl().setLookAt(MultiAttackMob.this.getTarget(), (float)MultiAttackMob.this.getMaxHeadYRot(), (float)MultiAttackMob.this.getMaxHeadXRot());
            }
        }
    }

    public abstract class TridotMeleeAttackGoal extends AttackGoal {
        public final MultiAttackMob mob;
        public final double speedModifier;
        public int ticksUntilNextPathRecalc;
        public double lastTargetX, lastTargetY, lastTargetZ;

        public TridotMeleeAttackGoal(MultiAttackMob mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isPreparingAttack();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && MultiAttackMob.this.attackWarmupDelay > 0;
        }

        public void start() {
            MultiAttackMob.this.setAggressive(true);
            MultiAttackMob.this.setCurrentAttack(this.getAttack());
            MultiAttackMob.this.preparingTickCount = this.getPreparingTime();
            this.nextAttackTickCount = MultiAttackMob.this.tickCount + this.getAttackInterval();
            this.onPrepare();
            this.ticksUntilNextPathRecalc = 0;
            this.mob.getNavigation().moveTo(mob.getTarget(), speedModifier);
            storeTargetPosition();
        }


        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (--ticksUntilNextPathRecalc <= 0 &&
            (distanceToStoredTargetSqr(target) >= 1.0D || mob.getRandom().nextFloat() < 0.05F)) {
                storeTargetPosition();
                this.ticksUntilNextPathRecalc += 5;
                mob.getNavigation().moveTo(target, speedModifier);
            }

            if (!mob.isPreparingAttack() && mob.attackWarmupDelay == 0 && mob.globalCooldown == 0 && mob.attackAnimationTick <= 5) {
                double distSq = this.mob.distanceToSqr(target);
                if(distSq > 1024.0D){
                    this.ticksUntilNextPathRecalc += 10;
                }else if(distSq > 256.0D){
                    this.ticksUntilNextPathRecalc += 5;
                }

                if (isWithinMeleeAttackRange(target)) {
                    mob.attackAnimationTick = 0;
                    mob.attackWarmupDelay = adjustedTickDelay(getPreparingTime());
                    mob.globalCooldown = 20;
                    mob.playSound(getAttackSound(), 1.0F, 1.0F);
                    onPrepare();
                }
            }
        }

        private void storeTargetPosition() {
            LivingEntity target = mob.getTarget();
            this.lastTargetX = target.getX();
            this.lastTargetY = target.getY();
            this.lastTargetZ = target.getZ();
        }

        private double distanceToStoredTargetSqr(LivingEntity target) {
            return target.distanceToSqr(lastTargetX, lastTargetY, lastTargetZ);
        }

        @Override
        protected void performAttack() {
            LivingEntity target = mob.getTarget();
            if (target != null && target.isAlive()) {
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(target);
            }
        }

        public abstract int attackAnimationTick();

        public void beforeAttack() {
            MultiAttackMob.this.attackAnimationTick = attackAnimationTick();
            SoundEvent soundevent = this.getPrepareSound();

            if (soundevent != null) {
                MultiAttackMob.this.playSound(soundevent, 1.0F, 1.0F);
            }
        }

        @Override
        public void onPrepare() {
            if (mob.getTarget() != null && isWithinMeleeAttackRange(mob.getTarget())) {
                beforeAttack();
            }
        }
    }

    public abstract class AttackGoal extends Goal{
        protected int nextAttackTickCount;

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse(){
            LivingEntity livingentity = MultiAttackMob.this.getTarget();
            if(MultiAttackMob.this.hasTarget() && livingentity.isAlive()){
                if(MultiAttackMob.this.isPreparingAttack()){
                    return false;
                }else{
                    return MultiAttackMob.this.tickCount >= this.nextAttackTickCount;
                }
            }else{
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse(){
            LivingEntity livingentity = MultiAttackMob.this.getTarget();
            return livingentity != null && livingentity.isAlive() && MultiAttackMob.this.attackWarmupDelay > 0;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start(){
            MultiAttackMob.this.setAggressive(true);
            MultiAttackMob.this.setCurrentAttack(this.getAttack());
            MultiAttackMob.this.preparingTickCount = this.getPreparingTime();
            this.nextAttackTickCount = MultiAttackMob.this.tickCount + this.getAttackInterval();
            SoundEvent soundevent = this.getPrepareSound();
            this.onPrepare();
            if(soundevent != null){
                MultiAttackMob.this.playSound(soundevent, 1.0F, 1.0F);
            }
        }

        @Override
        public void stop(){
            MultiAttackMob.this.setAggressive(false);
            super.stop();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick(){
            if(MultiAttackMob.this.attackWarmupDelay == 0 && MultiAttackMob.this.globalCooldown == 0){
                this.performAttack();
                MultiAttackMob.this.attackWarmupDelay = this.adjustedTickDelay(this.getPreparingTime());
                MultiAttackMob.this.globalCooldown = 20; // prevents attack spam
                MultiAttackMob.this.playSound(this.getAttackSound(), 1.0F, 1.0F);
            }
        }

        public abstract void onPrepare();

        protected abstract void performAttack();

        /**
         * Time to charge the attack
         */
        public abstract int getPreparingTime();

        /**
         * Cooldown between attacks
         */
        public abstract int getAttackInterval();

        @Nullable
        public abstract SoundEvent getPrepareSound();

        public SoundEvent getAttackSound(){
            return SoundEvents.EMPTY;
        }

        /**
         * Used to indicate which attack is used to delay it
         */
        public abstract AttackRegistry getAttack();
    }
}
