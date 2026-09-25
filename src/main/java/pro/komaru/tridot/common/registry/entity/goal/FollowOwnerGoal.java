package pro.komaru.tridot.common.registry.entity.goal;

import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.pathfinder.*;

import java.util.*;

public class FollowOwnerGoal extends Goal{
        private final Mob minion;
        private LivingEntity owner;
        private final LevelReader level;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;
        private float oldWaterCost;
        private final boolean canFly;

        public FollowOwnerGoal(Mob pTamable, double pSpeedModifier, float pStartDistance, float pStopDistance, boolean pCanFly) {
            this.minion = pTamable;
            this.level = pTamable.level();
            this.speedModifier = pSpeedModifier;
            this.navigation = pTamable.getNavigation();
            this.startDistance = pStartDistance;
            this.stopDistance = pStopDistance;
            this.canFly = pCanFly;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            if (!(pTamable.getNavigation() instanceof GroundPathNavigation) && !(pTamable.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if(!(minion instanceof OwnableEntity ownableEntity)) return false;
            LivingEntity livingentity = ownableEntity.getOwner();
            if (livingentity == null) {
                return false;
            } else if (livingentity.isSpectator()) {
                return false;
            } else if (this.minion.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else {
                return !(this.minion.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
            }
        }
        
        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.minion.getPathfindingMalus(PathType.WATER);
            this.minion.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.minion.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            this.minion.getLookControl().setLookAt(this.owner, 10.0F, (float)this.minion.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.minion.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = this.owner.blockPosition();
            for(int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }
        }

        private boolean maybeTeleportTo(int pX, int pY, int pZ) {
            if (Math.abs((double)pX - this.owner.getX()) < 2.0D && Math.abs((double)pZ - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
                return false;
            } else {
                this.minion.moveTo((double)pX + 0.5D, pY, (double)pZ + 0.5D, this.minion.getYRot(), this.minion.getXRot());
                this.navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pPos) {
            PathType blockpathtypes = WalkNodeEvaluator.getPathTypeStatic(this.minion, pPos.mutable()); // PORT NOTE: BlockPathTypes -> PathType; the static evaluator now takes the mob
            if (blockpathtypes != PathType.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = this.level.getBlockState(pPos.below());
                if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pPos.subtract(this.minion.blockPosition());
                    return this.level.noCollision(this.minion, this.minion.getBoundingBox().move(blockpos));
                }
            }
        }

        private int randomIntInclusive(int pMin, int pMax) {
            return this.minion.getRandom().nextInt(pMax - pMin + 1) + pMin;
        }
    }
