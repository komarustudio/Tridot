package pro.komaru.tridot.common.registry.entity.projectiles;

import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.network.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.api.EffectApplier;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.util.Log;

import java.util.*;

public abstract class AbstractTridotArrow extends AbstractArrow{
    public ItemStack arrowItem = ItemStack.EMPTY;
    private List<EffectApplier> effects = new ArrayList<>();

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level pLevel){
        super(pEntityType, pLevel);
    }

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level worldIn, LivingEntity thrower, double baseDamage){
        super(pEntityType, thrower, worldIn);
        this.baseDamage = baseDamage == 0 ? 2 : baseDamage;
    }

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level worldIn, LivingEntity thrower, ItemStack thrownStackIn, double baseDamage){
        super(pEntityType, thrower, worldIn);
        arrowItem = new ItemStack(thrownStackIn.getItem());
        this.baseDamage = baseDamage == 0 ? 2 : baseDamage;
    }

    public void doPostSpawn(){
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (Throwable t) {
            String msg = String.format(
                    "AbstractTridotArrow crashed! Class=%s, UUID=%s",
                    this.getClass().getName(),
                    this.getUUID()
            );

            Log.getLogger().error(msg, t);

            this.discard();
            return;
        }

        if (this.level().isClientSide() && !(this.isInWaterOrBubble() || this.isInWall())) {
            BlockPos below = this.blockPosition().below();
            BlockState state = this.level().getBlockState(below);

            if (!state.is(Blocks.BUBBLE_COLUMN)) this.spawnParticlesTrail();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(){
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void addEffect(MobEffectInstance pEffectInstance){
        this.effects.add(EffectApplier.of(pEffectInstance));
    }

    public void addAdditionalSaveData(@NotNull CompoundTag compound){
        super.addAdditionalSaveData(compound);
        if(!this.effects.isEmpty()){
            ListTag listtag = new ListTag();
            for(var applier : this.effects){
                listtag.add(applier.createInstance().save(new CompoundTag()));
            }

            compound.put("CustomPotionEffects", listtag);
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound){
        super.readAdditionalSaveData(pCompound);
        for(MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(pCompound)){
            this.addEffect(mobeffectinstance);
        }
    }

    protected void doPostHurtEffects(LivingEntity pLiving){
        super.doPostHurtEffects(pLiving);
        Utils.Entities.apply(pLiving, this.effects);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult){
        if (this.level().isClientSide()) return; // causes desync issues
        super.onHitEntity(pResult);
    }

    public void setEffectsFromList(ImmutableList<EffectApplier> effects){
        this.effects = new ArrayList<>(effects);
    }

    public void setEffectsFromList(EffectApplier... effects){
        this.effects = new ArrayList<>(List.of(effects));
    }

    @OnlyIn(Dist.CLIENT)
    public void spawnParticlesTrail(){
    }

    @Override
    public ItemStack getPickupItem(){
        return arrowItem;
    }
}