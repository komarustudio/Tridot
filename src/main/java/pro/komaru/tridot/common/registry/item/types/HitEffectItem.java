package pro.komaru.tridot.common.registry.item.types;

import com.google.common.collect.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.api.*;

import java.util.*;

public class HitEffectItem extends SwordItem{
    public final ImmutableList<EffectApplier> effects;

    public HitEffectItem(Tier tier, float attackDamageIn, float attackSpeedIn, Item.Properties builderIn, EffectApplier... pEffects){
        super(tier, (int)attackDamageIn, attackSpeedIn, builderIn);
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker){
        if(!(attacker instanceof Player player)) return true;
        stack.hurtAndBreak(2, attacker, (entity) -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        if(Utils.Items.getAttackStrengthScale(player, 0.9f)){
            Utils.Entities.apply(target, effects);
        }

        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags){
        super.appendHoverText(stack, world, tooltip, flags);
        Utils.Items.effectTooltip(effects, tooltip, 1);
    }
}