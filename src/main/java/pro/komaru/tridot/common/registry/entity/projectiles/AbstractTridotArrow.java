package pro.komaru.tridot.common.registry.entity.projectiles;

import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.util.Log;

import java.util.*;

public abstract class AbstractTridotArrow extends AbstractArrow{
    public ItemStack arrowItem = ItemStack.EMPTY;
    private final Set<MobEffectInstance> effects = Sets.newHashSet();

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level pLevel){
        super(pEntityType, pLevel);
    }

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level worldIn, LivingEntity thrower, double baseDamage){
        super(pEntityType, thrower, worldIn, ItemStack.EMPTY, null);
        this.baseDamage = baseDamage == 0 ? 2 : baseDamage;
    }

    public AbstractTridotArrow(EntityType<? extends AbstractArrow> pEntityType, Level worldIn, LivingEntity thrower, ItemStack thrownStackIn, double baseDamage){
        super(pEntityType, thrower, worldIn, new ItemStack(thrownStackIn.getItem()), null);
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

    public void addEffect(MobEffectInstance pEffectInstance){
        this.effects.add(pEffectInstance);
    }

    public void addAdditionalSaveData(@NotNull CompoundTag compound){
        super.addAdditionalSaveData(compound);
        if(!this.effects.isEmpty()){
            ListTag listtag = new ListTag();
            for(MobEffectInstance mobeffectinstance : this.effects){
                listtag.add(mobeffectinstance.save());
            }

            compound.put("CustomPotionEffects", listtag);
        }
    }

    public void readAdditionalSaveData(CompoundTag pCompound){
        super.readAdditionalSaveData(pCompound);
        for(MobEffectInstance mobeffectinstance : loadCustomEffects(pCompound)){
            this.addEffect(mobeffectinstance);
        }
    }

    public static List<MobEffectInstance> loadCustomEffects(CompoundTag compound){
        List<MobEffectInstance> list = new ArrayList<>();
        if(compound.contains("CustomPotionEffects", Tag.TAG_LIST)){
            ListTag listtag = compound.getList("CustomPotionEffects", Tag.TAG_COMPOUND);
            for(int i = 0; i < listtag.size(); i++){
                MobEffectInstance instance = MobEffectInstance.load(listtag.getCompound(i));
                if(instance != null) list.add(instance);
            }
        }
        return list;
    }

    protected void doPostHurtEffects(LivingEntity pLiving){
        super.doPostHurtEffects(pLiving);
        Entity entity = this.getEffectSource();
        if(!this.effects.isEmpty()){
            for(MobEffectInstance effect : this.effects){
                pLiving.addEffect(effect, entity);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult){
        if (this.level().isClientSide()) return; // causes desync issues
        super.onHitEntity(pResult);
    }

    public void setEffectsFromList(ImmutableList<MobEffectInstance> effects){
        for(MobEffectInstance mobeffectinstance : effects){
            this.effects.add(new MobEffectInstance(mobeffectinstance));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void spawnParticlesTrail(){
    }

    @Override
    public ItemStack getPickupItem(){
        return arrowItem;
    }

    @Override
    protected ItemStack getDefaultPickupItem(){
        return arrowItem == null || arrowItem.isEmpty() ? new ItemStack(Items.ARROW) : arrowItem;
    }
}
