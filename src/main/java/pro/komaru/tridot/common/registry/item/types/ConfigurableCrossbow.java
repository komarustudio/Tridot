package pro.komaru.tridot.common.registry.item.types;

import com.google.common.collect.*;
import net.minecraft.*;
import net.minecraft.advancements.*;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.stats.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.*;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;
import pro.komaru.tridot.common.registry.entity.projectiles.*;

import javax.annotation.*;
import java.lang.Math;
import java.util.*;
import java.util.function.*;

public class ConfigurableCrossbow extends CrossbowItem{
    public double baseDamage;
    public int arrowBaseDamage;
    public int chargeTime = 25;
    public int range = 8;

    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    public Supplier<? extends EntityType<? extends AbstractArrow>> arrow;

    public ConfigurableCrossbow(double pBaseDamage, Properties pProperties){
        super(pProperties);
        this.baseDamage = pBaseDamage;
        this.arrowBaseDamage = 2;
        this.arrow = () -> EntityType.ARROW;
    }

    public ConfigurableCrossbow(Supplier<? extends EntityType<? extends AbstractArrow>> arrow, double pBaseDamage, int pArrowBaseDamage, Properties pProperties){
        super(pProperties);
        this.baseDamage = pBaseDamage;
        this.arrowBaseDamage = pArrowBaseDamage;
        this.arrow = arrow;
    }

    public ConfigurableCrossbow(Supplier<? extends EntityType<? extends AbstractArrow>> arrow, int chargeTime, double pBaseDamage, int pArrowBaseDamage, Properties pProperties){
        super(pProperties);
        this.baseDamage = pBaseDamage;
        this.arrowBaseDamage = pArrowBaseDamage;
        this.arrow = arrow;
        this.chargeTime = chargeTime;
    }

    public @NotNull EntityType<? extends AbstractArrow> getDefaultType(){
        return arrow.get();
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (isCharged(itemstack)) {
            performCustomShooting(pLevel, pPlayer, pHand, itemstack, getShootingPower(itemstack), 1.0F);
            clearChargedProjectiles(itemstack);
            return InteractionResultHolder.consume(itemstack);
        } else if (!pPlayer.getProjectile(itemstack).isEmpty()) {
            if (!isCharged(itemstack)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                pPlayer.startUsingItem(pHand);
            }

            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    public float getShootingPower(ItemStack pCrossbowStack) {
        return containsChargedProjectile(pCrossbowStack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    public static boolean containsChargedProjectile(ItemStack pCrossbowStack, Item pAmmoItem) {
        return pCrossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).contains(pAmmoItem);
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        int i = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;
        float f = getPowerForTime(i, pStack, pEntityLiving);
        if (f >= 1.0F && !isCharged(pStack) && tryLoadProjectiles(pEntityLiving, pStack)) {
            SoundSource soundsource = pEntityLiving instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            pLevel.playSound(null, pEntityLiving.getX(), pEntityLiving.getY(), pEntityLiving.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    public boolean tryLoadProjectiles(LivingEntity pShooter, ItemStack pCrossbowStack) {
        int i = EnchantmentsRegistry.getLevel(pCrossbowStack, Enchantments.MULTISHOT);
        int j = i == 0 ? 1 : 3;
        boolean flag = pShooter instanceof Player && ((Player)pShooter).getAbilities().instabuild;
        ItemStack itemstack = pShooter.getProjectile(pCrossbowStack);
        ItemStack itemstack1 = itemstack.copy();

        for(int k = 0; k < j; ++k) {
            if (k > 0) {
                itemstack = itemstack1.copy();
            }

            if (itemstack.isEmpty() && flag) {
                itemstack = new ItemStack(Items.ARROW);
                itemstack1 = itemstack.copy();
            }

            if (!loadProjectile(pShooter, pCrossbowStack, itemstack, k > 0, flag)) {
                return false;
            }
        }

        return true;
    }

    public boolean loadProjectile(LivingEntity pShooter, ItemStack pCrossbowStack, ItemStack pAmmoStack, boolean pHasAmmo, boolean pIsCreative) {
        if (pAmmoStack.isEmpty()) {
            return false;
        } else {
            boolean flag = pIsCreative && pAmmoStack.getItem() instanceof ArrowItem;
            ItemStack itemstack;
            if (!flag && !pIsCreative && !pHasAmmo) {
                itemstack = pAmmoStack.split(1);
                if (pAmmoStack.isEmpty() && pShooter instanceof Player) {
                    ((Player)pShooter).getInventory().removeItem(pAmmoStack);
                }
            } else {
                itemstack = pAmmoStack.copy();
            }

            addChargedProjectile(pCrossbowStack, itemstack);
            return true;
        }
    }

    public void addChargedProjectile(ItemStack pCrossbowStack, ItemStack pAmmoStack) {
        List<ItemStack> list = new ArrayList<>(getChargedProjectiles(pCrossbowStack));
        list.add(pAmmoStack);
        pCrossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
    }

    public List<ItemStack> getChargedProjectiles(ItemStack pCrossbowStack) {
        return Lists.newArrayList(pCrossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).getItems());
    }

    public void clearChargedProjectiles(ItemStack pCrossbowStack) {
        pCrossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
    }

    public void shootProjectile(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pCrossbowStack, ItemStack pAmmoStack, float pSoundPitch, boolean pIsCreativeMode, float pVelocity, float pInaccuracy, float pProjectileAngle) {
        if (!pLevel.isClientSide) {
            boolean flag = pAmmoStack.is(Items.FIREWORK_ROCKET);
            Projectile projectile;
            if (flag) {
                projectile = new FireworkRocketEntity(pLevel, pAmmoStack, pShooter, pShooter.getX(), pShooter.getEyeY() - (double)0.15F, pShooter.getZ(), true);
            } else {
                projectile = getArrow(pLevel, pShooter, pCrossbowStack, pAmmoStack);
                if (pIsCreativeMode || pProjectileAngle != 0.0F) {
                    ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (pShooter instanceof CrossbowAttackMob crossbowattackmob && pShooter instanceof Mob mob && mob.getTarget() != null) {
                shootAtTarget(mob, mob.getTarget(), createArrow(pLevel, pShooter), pProjectileAngle);
                crossbowattackmob.onCrossbowAttackPerformed();
            } else {
                Vec3 vec31 = pShooter.getUpVector(1.0F);
                Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(pProjectileAngle * ((float)Math.PI / 180F), vec31.x, vec31.y, vec31.z);
                Vec3 vec3 = pShooter.getViewVector(1.0F);
                Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
                projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), pVelocity, pInaccuracy);
            }

            pCrossbowStack.hurtAndBreak(flag ? 3 : 1, pShooter, LivingEntity.getSlotForHand(pHand));
            pLevel.addFreshEntity(projectile);
            pLevel.playSound(null, pShooter.getX(), pShooter.getY(), pShooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pSoundPitch);
        }
    }

    private static void shootAtTarget(Mob user, LivingEntity target, Projectile projectile, float angle){
        double d0 = target.getX() - user.getX();
        double d1 = target.getZ() - user.getZ();
        double d2 = Math.sqrt(d0 * d0 + d1 * d1);
        double d3 = target.getY(0.3333333333333333D) - projectile.getY() + d2 * (double)0.2F;
        Vector3f vector3f = getProjectileShotVector(user, new Vec3(d0, d3, d1), angle);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), 1.6F, (float)(14 - user.level().getDifficulty().getId() * 4));
        user.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (user.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static Vector3f getProjectileShotVector(LivingEntity shooter, Vec3 distance, float angle){
        Vector3f vector3f = distance.toVector3f().normalize();
        Vector3f vector3f1 = new Vector3f(vector3f).cross(new Vector3f(0.0F, 1.0F, 0.0F));
        if((double)vector3f1.lengthSquared() <= 1.0E-7D){
            Vec3 vec3 = shooter.getUpVector(1.0F);
            vector3f1 = new Vector3f(vector3f).cross(vec3.toVector3f());
        }

        Vector3f vector3f2 = new Vector3f(vector3f).rotateAxis(((float)Math.PI / 2F), vector3f1.x, vector3f1.y, vector3f1.z);
        return new Vector3f(vector3f).rotateAxis(angle * ((float)Math.PI / 180F), vector3f2.x, vector3f2.y, vector3f2.z);
    }

    public void doPreSpawn(AbstractArrow abstractarrow, LivingEntity player, float power){
        abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + baseDamage);
        abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
        if(power == 1.0F){
            abstractarrow.setCritArrow(true);
        }
    }

    public AbstractArrow createArrow(Level pLevel, LivingEntity player){
        AbstractArrow customArrow = arrow.get().create(pLevel);
        customArrow.moveTo(new Vec3(player.getEyePosition().x, player.getEyePosition().y - 0.1f, player.getEyePosition().z));
        customArrow.setOwner(player);
        if(customArrow instanceof AbstractTridotArrow valor) valor.doPostSpawn();
        return customArrow;
    }

    public AbstractArrow getArrow(Level pLevel, LivingEntity pLivingEntity, ItemStack pCrossbowStack, ItemStack pAmmoStack) {
        ArrowItem arrowitem = (ArrowItem)(pAmmoStack.getItem() instanceof ArrowItem ? pAmmoStack.getItem() : Items.ARROW);
        AbstractArrow abstractarrow = arrowitem == Items.ARROW && arrow.get() != EntityType.ARROW ? createArrow(pLevel, pLivingEntity) : arrowitem.createArrow(pLevel, pAmmoStack, pLivingEntity, pCrossbowStack);
        if(pLivingEntity instanceof CrossbowAttackMob) {
            if(arrow.get() != EntityType.ARROW) createArrow(pLevel, pLivingEntity);
        }

        abstractarrow.setOwner(pLivingEntity);
        doPreSpawn(abstractarrow, pLivingEntity, getShootingPower(pCrossbowStack));
        if (pLivingEntity instanceof Player) {
            abstractarrow.setCritArrow(true);
        }

        abstractarrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        ConfigurableBowItem.applyWeaponEnchantments(pLevel, abstractarrow, pCrossbowStack);
        int i = EnchantmentsRegistry.getLevel(pCrossbowStack, Enchantments.PIERCING);
        if (i > 0) {
            abstractarrow.setPierceLevel((byte)i);
        }

        return abstractarrow;
    }

    public void performCustomShooting(Level pLevel, LivingEntity pShooter, InteractionHand pUsedHand, ItemStack pCrossbowStack, float pVelocity, float pInaccuracy) {
        if (pShooter instanceof Player player && EventHooks.onArrowLoose(pCrossbowStack, pShooter.level(), player, 1, true) < 0) return;
        List<ItemStack> list = getChargedProjectiles(pCrossbowStack);
        float[] afloat = getShotPitches(pShooter.getRandom());

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            boolean flag = pShooter instanceof Player && ((Player)pShooter).getAbilities().instabuild;
            if (!itemstack.isEmpty()) {
                if (i == 0) {
                    shootProjectile(pLevel, pShooter, pUsedHand, pCrossbowStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, 0.0F);
                } else if (i == 1) {
                    shootProjectile(pLevel, pShooter, pUsedHand, pCrossbowStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, -10.0F);
                } else if (i == 2) {
                    shootProjectile(pLevel, pShooter, pUsedHand, pCrossbowStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, 10.0F);
                }
            }
        }

        onCrossbowShot(pLevel, pShooter, pCrossbowStack);
    }

    @Override
    public void performShooting(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pWeapon, float pVelocity, float pInaccuracy, @Nullable LivingEntity pTarget){
        performCustomShooting(pLevel, pShooter, pHand, pWeapon, pVelocity, pInaccuracy);
    }

    public float[] getShotPitches(RandomSource pRandom) {
        boolean flag = pRandom.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, pRandom), getRandomShotPitch(!flag, pRandom)};
    }

    public float getRandomShotPitch(boolean pIsHighPitched, RandomSource pRandom) {
        float f = pIsHighPitched ? 0.63F : 0.43F;
        return 1.0F / (pRandom.nextFloat() * 0.5F + 1.8F) + f;
    }

    public void onCrossbowShot(Level pLevel, LivingEntity pShooter, ItemStack pCrossbowStack) {
        if (pShooter instanceof ServerPlayer serverplayer) {
            if (!pLevel.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverplayer, pCrossbowStack);
            }

            serverplayer.awardStat(Stats.ITEM_USED.get(pCrossbowStack.getItem()));
        }

        clearChargedProjectiles(pCrossbowStack);
    }

    /**
     * Called as the item is being used by an entity.
     */
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pCount) {
        if (!pLevel.isClientSide) {
            int i = EnchantmentsRegistry.getLevel(pStack, Enchantments.QUICK_CHARGE);
            SoundEvent soundevent = this.getStartSound(i);
            SoundEvent soundevent1 = i == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE.value() : null;
            float f = (float)(pStack.getUseDuration(pLivingEntity) - pCount) / (float)getCustomChargeDuration(pStack);
            if (f < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && soundevent1 != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent1, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return getCustomChargeDuration(pStack) + 3;
    }

    /**
     * The time the crossbow must be used to reload it
     */
    public int getCustomChargeDuration(ItemStack pCrossbowStack) {
        int i = EnchantmentsRegistry.getLevel(pCrossbowStack, Enchantments.QUICK_CHARGE);
        return i == 0 ? chargeTime : chargeTime - 5 * i;
    }

    public SoundEvent getStartSound(int pEnchantmentLevel) {
        return switch(pEnchantmentLevel){
            case 1 -> SoundEvents.CROSSBOW_QUICK_CHARGE_1.value();
            case 2 -> SoundEvents.CROSSBOW_QUICK_CHARGE_2.value();
            case 3 -> SoundEvents.CROSSBOW_QUICK_CHARGE_3.value();
            default -> SoundEvents.CROSSBOW_LOADING_START.value();
        };
    }

    public float getPowerForTime(int pUseTime, ItemStack pCrossbowStack) {
        float f = (float)pUseTime / (float)getCustomChargeDuration(pCrossbowStack);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public float getPowerForTime(int pUseTime, ItemStack pCrossbowStack, LivingEntity pShooter) {
        return getPowerForTime(pUseTime, pCrossbowStack);
    }

    private double calculateAverageDamage(ItemStack pStack){
        double baseArrowDamage = this.baseDamage + 2;
        int powerLevel = EnchantmentsRegistry.getLevel(pStack, Enchantments.POWER);
        double powerBonus = powerLevel > 0 ? (powerLevel * 0.5D + 0.5D) : 0.0D;
        return (baseArrowDamage + powerBonus) * (2 * 2.0F) - 2;
    }

    /**
     * Allows items to add custom lines of information to the mouseover description.
     */
    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag) {
        List<ItemStack> list = getChargedProjectiles(pStack);
        if (isCharged(pStack) && !list.isEmpty()) {
            ItemStack itemstack = list.get(0);
            Component name = itemstack.is(Items.ARROW) && arrow.get() != EntityType.ARROW ? arrow.get().getDescription() : itemstack.getDisplayName();
            pTooltip.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(name));
            if (pFlag.isAdvanced() && itemstack.is(Items.FIREWORK_ROCKET)) {
                List<Component> list1 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemstack, pContext, list1, pFlag);
                if (!list1.isEmpty()) {
                    list1.replaceAll(pSibling -> Component.literal("  ").append(pSibling).withStyle(ChatFormatting.GRAY));
                    pTooltip.addAll(list1);
                }
            }
        }

        pTooltip.add(Component.translatable("tooltip.tridot.crossbow.speed", Utils.Items.formatTickDuration(getCustomChargeDuration(pStack))).withStyle(ChatFormatting.GRAY));
        double damage = calculateAverageDamage(pStack);
        if(arrow.get() != EntityType.ARROW){
            pTooltip.add(Component.translatable("tooltip.tridot.special_arrow").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(getDefaultType().getDescription().getString()).withStyle(pStack.getRarity().getStyleModifier())));
        }

        pTooltip.add(Component.translatable("tooltip.tridot.bow_damage", Math.floor(damage)).withStyle(ChatFormatting.GRAY));

    }

    // used by entities
    public int getDefaultProjectileRange() {
        return range;
    }
}