package pro.komaru.tridot.common.registry.item.types;

import net.minecraft.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.inventory.tooltip.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.api.interfaces.*;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.common.registry.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.builders.*;
import pro.komaru.tridot.common.registry.item.components.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.struct.data.*;

import java.util.*;

public class ConfiguredShield extends ShieldItem implements TooltipComponentItem, CooldownReductionItem {
    public AbstractShieldBuilder<? extends ConfiguredShield> builder;

    public ConfiguredShield(AbstractShieldBuilder<? extends ConfiguredShield> builder){
        super(builder.itemProperties);
        this.builder = builder;
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    @Override
    public int getEnchantmentValue() {
        return builder.tier.getEnchantmentValue();
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    @Override
    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return builder.tier.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltip, TooltipFlag pFlag){
        super.appendHoverText(pStack, pContext, pTooltip, pFlag);
        pTooltip.add(Component.translatable("tooltip.tridot.shield.block", String.format("%.1f%%", builder.blockedPercent * 100)).withStyle(ChatFormatting.GRAY));
        if(!builder.infiniteUse) pTooltip.add(Component.translatable("tooltip.tridot.shield.time", formatDuration(builder.useDuration)).withStyle(ChatFormatting.GRAY));
        if(!pStack.isDamageableItem()){
            pTooltip.add(Component.empty());
            pTooltip.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
        }

        if(builder.returnedDamagePercent > 0) {
            pTooltip.add(Component.translatable("tooltip.tridot.shield.thorns", String.format("%.1f%%", builder.returnedDamagePercent * 100)).withStyle(ChatFormatting.GRAY));
        }

        addEffectTooltip(pTooltip, builder.attackerBlockEffects, "attacker.block");
        addEffectTooltip(pTooltip, builder.defenderBlockEffects, "defender.block");
        addEffectTooltip(pTooltip, builder.attackerParryEffects, "attacker.parry");
        addEffectTooltip(pTooltip, builder.defenderParryEffects, "defender.parry");
        addEffectTooltip(pTooltip, builder.attackerShieldDisableEffects , "attacker.disable");
        addEffectTooltip(pTooltip, builder.defenderShieldDisableEffects , "defender.disable");
    }

    private void addEffectTooltip(List<Component> pTooltip, EffectList effectData, String key) {
        var effects = effectData.getEffects();
        if (effects.isEmpty()) return;

        var chance = effectData.getChance();
        pTooltip.add(Component.empty());
        if (chance > 0 && chance < 1) {
            pTooltip.add(Component.translatable("tooltip.tridot.applies_with_chance_to_" + key, String.format("%.1f%%", chance * 100)).withStyle(ChatFormatting.GRAY));
        } else pTooltip.add(Component.translatable("tooltip.tridot.applies_to_" + key).withStyle(ChatFormatting.GRAY));

        Utils.Items.effectLines(effects, pTooltip, 1);
    }

    public Component formatDuration(int useDuration) {
        int i = Mth.floor((float)useDuration);
        return Component.literal(StringUtil.formatTickDuration(i, 20.0F));
    }

    public int getParryWindow(ItemStack stack) {
        int lvl = EnchantmentsRegistry.getLevel(stack, EnchantmentsRegistry.VIGILANCE);
        return Math.round(builder.parryWindow + (lvl * 1.5f));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (builder.infiniteUse) return 72000;
        int lvl = EnchantmentsRegistry.getLevel(stack, EnchantmentsRegistry.IRON_GRIP);
        return builder.useDuration + (lvl * 20);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!(pLivingEntity instanceof Player player)) return;

        var ticks = builder.onShieldReleaseTicks;
        if (!player.getCooldowns().isOnCooldown(this) && ticks != 0) {
            applyCooldown(player, ticks, false);
        }

        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
    }

    public void onShieldDisable(ItemStack itemStack, Level level, Player player, @Nullable LivingEntity attacker, boolean pBecauseOfAxe) {
        if(!pBecauseOfAxe) return;

        Utils.Entities.applyWithChance(player, builder.defenderShieldDisableEffects.getEffects(), builder.defenderShieldDisableEffects.getChance(), Tmp.rnd);
        if (attacker != null) Utils.Entities.applyWithChance(attacker, builder.attackerShieldDisableEffects.getEffects(), builder.attackerShieldDisableEffects.getChance(), Tmp.rnd);
    }

    public static float shieldDisableChance(Player player, boolean becauseOfAxe){
        return 0.25F + (becauseOfAxe ? 0.75F : 0.0F);
    }

    public float onPostBlock(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity, float armor){
        if (source.getEntity() instanceof LivingEntity attacker && entity instanceof Player player){
            var pMobItemStack = attacker.getMainHandItem(); // the weapon
            var pPlayerItemStack = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
            if (!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && (pMobItemStack.getItem().canDisableShield(pPlayerItemStack, itemStack, entity, attacker) || pMobItemStack.is(TagsRegistry.CAN_DISABLE_SHIELD))){
                float f = shieldDisableChance(player, false);
                if (attacker instanceof Player attackingPlayer) {
                    float attackStrength = attackingPlayer.getAttackStrengthScale(0.5F);
                    if (attackStrength < 0.9F) return armor;
                }

                if(Tmp.rnd.nextFloat() < f){
                    onShieldDisable(itemStack, entity.level(), player, attacker, false);
                    disableShield(player, true);
                    return 0;
                }
            }
        }

        return armor;
    }

    public void onShieldBlock(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity){
        var attacker = source.getEntity();
        if(attacker != null) {
            if (attacker instanceof LivingEntity livingAttacker) {
                Utils.Entities.applyWithChance(livingAttacker, builder.attackerBlockEffects.getEffects(), builder.attackerBlockEffects.getChance(), Tmp.rnd);
            }
        }

        Utils.Entities.applyWithChance(entity, builder.defenderBlockEffects.getEffects(), builder.defenderBlockEffects.getChance(), Tmp.rnd);
        if(source.getDirectEntity() != null){
            source.getDirectEntity().hurt(entity.level().damageSources().thorns(entity), pAmount * builder.returnedDamagePercent);
        }
    }

    @Override
    public Seq<TooltipComponent> getTooltips(ItemStack pStack) {
        if(builder.canParry) {
            return Seq.with(
                    new SeparatorComponent(Component.translatable("tooltip.tridot.abilities")),
                    new AbilityComponent(Component.translatable("tooltip.tridot.parry").withStyle(ChatFormatting.GRAY), Tridot.ofTridot("textures/gui/tooltips/parry.png")),
                    new TextComponent(Component.translatable("tooltip.tridot.parry_window", (getParryWindow(pStack) / 20f)).withStyle(ChatFormatting.GRAY)),
                    new EmptyComponent(12)
            );
        }

        return Seq.with();
    }

    public void onParry(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity) {
        var level = entity.level();
        int resonanceLvl = EnchantmentsRegistry.getLevel(itemStack, EnchantmentsRegistry.RESONANCE);
        if (entity instanceof Player player) {
            if (resonanceLvl == 0 && player.getCooldowns().isOnCooldown(itemStack.getItem())) return;

            int deflectLvl = EnchantmentsRegistry.getLevel(itemStack, EnchantmentsRegistry.DEFLECT);
            Entity directEntity = source.getDirectEntity();
            if (directEntity instanceof Projectile projectile && deflectLvl > 0) {
                if (projectile instanceof AbstractArrow arrow) {
                    byte pierceLevel = arrow.getPierceLevel();
                    if (pierceLevel > 0) {
                        arrow.setPierceLevel((byte) (pierceLevel - 1));
                    }
                }

                Vec3 reboundAngle = player.getLookAngle();
                projectile.setDeltaMovement(reboundAngle);
                // i hope it will prevent most of the issues that can appear
                if (projectile instanceof AbstractHurtingProjectile hurtingProjectile) {
                    hurtingProjectile.accelerationPower = 0.1D;
                    hurtingProjectile.setOwner(player);
                }

                projectile.hurtMarked = true;
            }

            var attacker = source.getEntity();
            if(attacker != null) {
                attacker.hurt(player.damageSources().thorns(player), pAmount * 0.25f);
                int pushLvl = EnchantmentsRegistry.getLevel(itemStack, EnchantmentsRegistry.PUSH);
                float knockbackStrength = Math.min(0.6F + (pushLvl * 0.3F), 2.5F);
                if (attacker instanceof LivingEntity livingAttacker) {
                    Utils.Entities.applyWithChance(livingAttacker, builder.attackerParryEffects.getEffects(), builder.attackerParryEffects.getChance(), Tmp.rnd);
                    livingAttacker.knockback(knockbackStrength * 0.5F, Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));
                } else attacker.push(-Mth.sin(player.getYRot() * ((float) Math.PI / 180F)) * knockbackStrength * 0.5F, 0.1D, Mth.cos(player.getYRot() * ((float) Math.PI / 180F)) * knockbackStrength * 0.5F);

                attacker.hurtMarked = true;
            }

            if (builder.parrySound != null) level.playSound(null, player.blockPosition(), builder.parrySound, SoundSource.PLAYERS);
            if (player instanceof ServerPlayer server) PacketHandler.sendTo(server, new ParryParticlePacket(entity.getX(), entity.getY() + 0.5f, entity.getZ()));

            player.invulnerableTime = 20;
            onShieldDisable(itemStack, level, player, null, false);
            applyCooldown(player, builder.parryCooldownTicks, false);
            if(resonanceLvl == 0) player.stopUsingItem();
            Utils.Entities.applyWithChance(player, builder.defenderParryEffects.getEffects(), builder.defenderParryEffects.getChance(), Tmp.rnd);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof Player player && !player.level().isClientSide) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                applyCooldown(player, builder.onShieldReleaseTicks, false); // prevents shield parry abuse
            }
        }
    }

    public void applyCooldown(Player playerIn, int cooldownTime, boolean applyReduction){
        for(Item item : BuiltInRegistries.ITEM){
            if(item instanceof ShieldItem){
                playerIn.getCooldowns().addCooldown(item, applyReduction ? getCooldownReduction(cooldownTime, playerIn.getUseItem()) : cooldownTime);
            }
        }
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !builder.infiniteUse) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS);
            itemStack.hurtAndBreak((int) (itemStack.getMaxDamage()*0.075f), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            applyCooldown(player, builder.cooldownTicks, true);
            player.stopUsingItem();
            onShieldDisable(itemStack, level, player, null, false);
        }

        return super.finishUsingItem(itemStack, level, entity);
    }

    public void disableShield(Player player, boolean pBecauseOfAxe) {
        float f = shieldDisableChance(player, pBecauseOfAxe);

        if (Tmp.rnd.nextFloat() < f) {
            applyCooldown(player, builder.shieldDisableTicks, false);
            player.stopUsingItem();

            var level = player.level();
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F + Tmp.rnd.nextFloat() * 0.4F);
            level.broadcastEntityEvent(player, (byte)30);
        }

    }

    public static class Builder extends AbstractShieldBuilder<ConfiguredShield>{
        public Builder(Properties itemProperties) {
            super(itemProperties);
        }

        public Builder(float defPercent, Properties itemProperties) {
            super(defPercent, itemProperties);
        }

        @Override
        public ConfiguredShield build(){
            return new ConfiguredShield(this);
        }
    }

}
