package pro.komaru.tridot.common.registry.item.types;

import com.google.common.collect.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;

import java.util.*;

public class HitEffectItem extends SwordItem{
    public float chance = 1;
    public final ImmutableList<MobEffectInstance> effects;
    public ArcRandom arcRandom = Tmp.rnd;

    public HitEffectItem(Tier tier, float attackDamageIn, float attackSpeedIn, Item.Properties builderIn, float pChance, MobEffectInstance... pEffects){
        super(tier, builderIn.attributes(SwordItem.createAttributes(tier, (int)attackDamageIn, attackSpeedIn)));
        this.chance = pChance;
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public HitEffectItem(Tier tier, float attackDamageIn, float attackSpeedIn, Item.Properties builderIn, MobEffectInstance... pEffects){
        super(tier, builderIn.attributes(SwordItem.createAttributes(tier, (int)attackDamageIn, attackSpeedIn)));
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker){
        if(!(attacker instanceof Player player)) return true;
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
        if(Utils.Items.getAttackStrengthScale(player, 0.9f)){
            Utils.Entities.applyWithChance(target, effects, chance, arcRandom);
        }

        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags){
        super.appendHoverText(stack, context, tooltip, flags);
        Utils.Items.effectTooltip(effects, tooltip, 1, chance);
    }
}