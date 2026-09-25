package pro.komaru.tridot.common.registry.entity;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ai.targeting.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.scores.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.api.interfaces.*;

import javax.annotation.*;
import java.awt.*;
import java.util.*;

public abstract class AbstractMinionEntity extends Monster implements OwnableEntity, Allied{
    @Nullable
    public BlockPos boundOrigin;
    public boolean hasLimitedLife;
    public int limitedLifeTicks;
    public static final Map<EntityType<? extends AbstractMinionEntity>, Color> minionColors = new HashMap<>();
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(AbstractMinionEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected AbstractMinionEntity(EntityType<? extends Monster> pEntityType, Level pLevel){
        super(pEntityType, pLevel);
    }

    public void tick(){
        super.tick();
        this.setNoGravity(true);
        if(this.hasLimitedLife && --this.limitedLifeTicks <= 0){
            if(this.level() instanceof ServerLevel serv){
                spawnDisappearParticles(serv);
                this.remove(RemovalReason.KILLED);
            }
        }

        if(this.shouldRenderAtSqrDistance(4) && this.level().isClientSide){
            spawnParticlesTrail();
        }
    }

    /**
     * Server sided
     */
    public void spawnDisappearParticles(ServerLevel serverLevel){
    }

    public void spawnParticlesTrail(){
    }

    public static Color getColor(EntityType<? extends AbstractMinionEntity> entityType){
        return minionColors.getOrDefault(entityType, Color.WHITE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound){
        super.readAdditionalSaveData(pCompound);
        if(pCompound.contains("BoundX")){
            this.boundOrigin = new BlockPos(pCompound.getInt("BoundX"), pCompound.getInt("BoundY"), pCompound.getInt("BoundZ"));
        }

        UUID uuid;
        if (pCompound.hasUUID("Owner")) {
            uuid = pCompound.getUUID("Owner");
        } else {
            String s = pCompound.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable ignored){}
        }

        if(pCompound.contains("LifeTicks")){
            this.setLimitedLife(pCompound.getInt("LifeTicks"));
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound){
        super.addAdditionalSaveData(pCompound);
        if(this.boundOrigin != null){
            pCompound.putInt("BoundX", this.boundOrigin.getX());
            pCompound.putInt("BoundY", this.boundOrigin.getY());
            pCompound.putInt("BoundZ", this.boundOrigin.getZ());
        }

        if (this.getOwnerUUID() != null) {
            pCompound.putUUID("Owner", this.getOwnerUUID());
        }

        if(this.hasLimitedLife){
            pCompound.putInt("LifeTicks", this.limitedLifeTicks);
        }
    }

    public boolean isOwned() {
        return this.getOwner() != null;
    }

    public PlayerTeam getTeam() {
        if (this.isOwned()) {
            LivingEntity livingentity = this.getOwner();
            if (livingentity != null) {
                return livingentity.getTeam();
            }
        }

        return super.getTeam();
    }

    public boolean isOwnedBy(LivingEntity pEntity) {
        return pEntity == this.getOwner();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID pUuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(pUuid));
    }

    public void setOwner(LivingEntity pOwner){
        this.setOwnerUUID(pOwner.getUUID());
    }

    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount){
        if(pSource.getDirectEntity() == this.getOwner() && ((this.getOwner() != null) && !this.getOwner().isShiftKeyDown())) return false;
        if(pSource.getDirectEntity() instanceof Allied) return false;
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected boolean shouldDropLoot(){
        return super.shouldDropLoot() && this.getOwner() == null;
    }

    @Override
    public boolean isAlliedTo(Entity pEntity){
        return super.isAlliedTo(pEntity) || pEntity instanceof Allied || (pEntity instanceof LivingEntity target && this.isOwnedBy(target));
    }

    public boolean canAttack(LivingEntity pTarget){
        boolean flag = !this.isOwnedBy(pTarget) || !isAlliedTo(pTarget);
        return super.canAttack(pTarget) && (flag || (this.getOwner() != null && this.getOwner().canAttack(pTarget)));
    }

    @Nullable
    public BlockPos getBoundOrigin(){
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos pBoundOrigin){
        this.boundOrigin = pBoundOrigin;
    }

    public void setLimitedLife(int pLimitedLifeTicks){
        this.hasLimitedLife = true;
        this.limitedLifeTicks = pLimitedLifeTicks;
    }
}