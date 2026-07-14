package pro.komaru.tridot.common.registry.item.types;

import net.minecraft.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.interfaces.CooldownReductionItem;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.client.render.gui.overlay.OverlayHandler;
import pro.komaru.tridot.client.render.gui.overlay.TimedOverlayInstance;
import pro.komaru.tridot.client.render.screenshake.PositionedScreenshakeInstance;
import pro.komaru.tridot.client.render.screenshake.ScreenshakeHandler;
import pro.komaru.tridot.common.networking.packets.ParryParticlePacket;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;
import pro.komaru.tridot.common.registry.item.TooltipComponentItem;
import pro.komaru.tridot.common.registry.item.components.AbilityComponent;
import pro.komaru.tridot.common.registry.item.components.EmptyComponent;
import pro.komaru.tridot.common.registry.item.components.SeparatorComponent;
import pro.komaru.tridot.common.registry.item.components.TextComponent;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.math.Interp;
import pro.komaru.tridot.util.struct.data.Seq;

import java.util.*;

public class ConfiguredShield extends ShieldItem implements TooltipComponentItem, CooldownReductionItem {
    public boolean infiniteUse = true;
    public float blockedPercent = 100;
    public int useDuration;
    public int cooldownTicks = 135;
    public int parryWindow = 10;
    public boolean canParry = true;

    public ConfiguredShield(Properties pProperties){
        super(pProperties);
    }

    public ConfiguredShield(float defPercent, Properties pProperties){
        this(pProperties);
        this.blockedPercent = defPercent;
    }

    public ConfiguredShield(float defPercent, int useDuration, Properties pProperties){
        this(pProperties);
        this.blockedPercent = defPercent;
        this.useDuration = useDuration;
        this.infiniteUse = false;
    }

    public ConfiguredShield(float defPercent, int useDuration, int cooldown, Properties pProperties){
        this(pProperties);
        this.blockedPercent = defPercent;
        this.useDuration = useDuration;
        this.cooldownTicks = cooldown;
        this.infiniteUse = false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag){
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        pTooltip.add(Component.translatable("tooltip.tridot.shield.block", String.format("%.1f%%", this.blockedPercent)).withStyle(ChatFormatting.GRAY));
        if(!this.infiniteUse) pTooltip.add(Component.translatable("tooltip.tridot.shield.time", formatDuration(this.useDuration)).withStyle(ChatFormatting.GRAY));
        if(!pStack.getItem().canBeDepleted()){
            pTooltip.add(Component.empty());
            pTooltip.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
        }
    }   

    public Component formatDuration(int useDuration) {
        int i = Mth.floor((float)useDuration);
        return Component.literal(StringUtil.formatTickDuration(i));
    }

    public int getParryWindow(ItemStack stack) {
        int lvl = stack.getEnchantmentLevel(EnchantmentsRegistry.VIGILANCE.get());
        return this.parryWindow + (lvl * 4);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (infiniteUse) return 72000;
        int lvl = stack.getEnchantmentLevel(EnchantmentsRegistry.IRON_GRIP.get());
        return this.useDuration + (lvl * 20);
    }

    public void onShieldDisable(ItemStack itemStack,Level level, Player player) {
    }

    public float onPostBlock(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity, float armor){
        if(source.getEntity() instanceof LivingEntity attacker && entity instanceof Player player){
            var pMobItemStack = attacker.getMainHandItem(); // the weapon
            var pPlayerItemStack = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
            if(!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && pMobItemStack.getItem() instanceof AxeItem){
                float f = 0.25F + (float) EnchantmentHelper.getBlockEfficiency(player) * 0.05F;
                if (attacker instanceof Player attackingPlayer) {
                    float attackStrength = attackingPlayer.getAttackStrengthScale(0.5F);
                    if (attackStrength < 0.9F) {
                        return armor;
                    }
                }

                if(Tmp.rnd.nextFloat() < f){
                    player.getCooldowns().addCooldown(itemStack.getItem(), cooldownTicks);
                    player.level().broadcastEntityEvent(player, (byte)30);
                    return 0;
                }
            }
        }

        return armor;
    }

    public void onShieldBlock(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity){
    }

    @Override
    public Seq<TooltipComponent> getTooltips(ItemStack pStack) {
        if(canParry) {
            return Seq.with(
                    new SeparatorComponent(Component.translatable("tooltip.tridot.abilities")),
                    new AbilityComponent(Component.translatable("tooltip.tridot.parry").withStyle(ChatFormatting.GRAY), Tridot.ofTridot("textures/gui/tooltips/parry.png")),
                    new TextComponent(Component.translatable("tooltip.tridot.parry_window", getParryWindow(pStack)).withStyle(ChatFormatting.GRAY)),
                    new EmptyComponent(12)
            );
        }

        return Seq.with();
    }

    @Nullable
    public SoundEvent parrySound() {
        return SoundEvents.SHIELD_BREAK;
    }

    public void onParry(DamageSource source, float pAmount, ItemStack itemStack, LivingEntity entity) {
        var level = entity.level();
        int resonanceLvl = itemStack.getEnchantmentLevel(EnchantmentsRegistry.RESONANCE.get());
        if (entity instanceof Player player) {
            if (resonanceLvl == 0 && player.getCooldowns().isOnCooldown(itemStack.getItem())) return;

            int deflectLvl = itemStack.getEnchantmentLevel(EnchantmentsRegistry.DEFLECT.get());
            Entity directEntity = source.getDirectEntity();
            if (directEntity instanceof Projectile projectile && deflectLvl > 0) {
                if (projectile instanceof AbstractArrow arrow) {
                    byte pierceLevel = arrow.getPierceLevel();
                    if (pierceLevel > 0) {
                        arrow.setPierceLevel((byte) (pierceLevel - 1));
                    }
                }

                projectile.hurtMarked = true;
                Vec3 reboundAngle = player.getLookAngle();
                projectile.setDeltaMovement(reboundAngle);

                // i hope it will prevent most of the issues that can appear
                if (projectile instanceof AbstractHurtingProjectile hurtingProjectile) {
                    hurtingProjectile.xPower = reboundAngle.x * 0.1D;
                    hurtingProjectile.yPower = reboundAngle.y * 0.1D;
                    hurtingProjectile.zPower = reboundAngle.z * 0.1D;
                    hurtingProjectile.setOwner(player);
                } else if (projectile instanceof ThrownPotion) {
                    projectile.setOwner(player);
                }
            }

            if (source.getEntity() instanceof LivingEntity attacker) {
                attacker.hurt(player.damageSources().thorns(player), pAmount * 0.25f);
                int pushLvl = itemStack.getEnchantmentLevel(EnchantmentsRegistry.PUSH.get());
                float knockbackStrength = Math.min(0.6F + (pushLvl * 0.3F), 2.5F);
                double ratioX = attacker.getX() - player.getX();
                double ratioZ = attacker.getZ() - player.getZ();
                attacker.knockback(knockbackStrength, ratioX, ratioZ);
                attacker.hurtMarked = true;
                if (parrySound() != null) level.playSound(null, player.blockPosition(), parrySound(), SoundSource.PLAYERS);
            }

            if (level instanceof ServerLevel server) {
                PacketHandler.sendToTracking(server, entity.blockPosition(), new ParryParticlePacket(entity.getX(), entity.getY() + 0.5f, entity.getZ()));
            }

            ScreenshakeHandler.add(new PositionedScreenshakeInstance(20, Pos3.init((float) entity.getX(), (float) entity.getY(), (float) entity.getZ()), 0, 3, Interp.elastic).interp(Interp.fade).intensity(2));
            player.invulnerableTime = 20;
            player.getCooldowns().addCooldown(itemStack.getItem(), getCooldownReduction(cooldownTicks, itemStack));
        }
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !infiniteUse) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS);
            itemStack.hurtAndBreak((int) (itemStack.getMaxDamage()*0.075f), player, (p1) -> p1.broadcastBreakEvent(player.getUsedItemHand()));
            for (Item item : ForgeRegistries.ITEMS) {
                if(item instanceof ConfiguredShield) {
                    player.getCooldowns().addCooldown(item, cooldownTicks);
                    onShieldDisable(itemStack, level, player);
                    player.disableShield(false);
                }
            }
        }

        return super.finishUsingItem(itemStack, level, entity);
    }
}