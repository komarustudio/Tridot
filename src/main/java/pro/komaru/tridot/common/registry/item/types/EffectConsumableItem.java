package pro.komaru.tridot.common.registry.item.types;

import com.google.common.collect.*;
import net.minecraft.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.tooltip.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraftforge.registries.*;
import pro.komaru.tridot.api.EffectApplier;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.components.*;
import pro.komaru.tridot.util.struct.data.*;

import java.util.ArrayList;
import java.util.List;

public class EffectConsumableItem extends AbstractConsumableItem implements TooltipComponentItem{
    private final ImmutableList<EffectApplier> effects;
    public int cooldownTicks = 75;

    public EffectConsumableItem(Properties pProperties, EffectApplier... pEffects){
        super(pProperties);
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public EffectConsumableItem(int drinkDuration, Properties pProperties, EffectApplier... pEffects){
        super(pProperties);
        this.effects = ImmutableList.copyOf(pEffects);
        this.useDuration = drinkDuration;
    }

    public EffectConsumableItem(int cooldownTicks, int drinkDuration, Properties pProperties, EffectApplier... pEffects){
        super(pProperties);
        this.effects = ImmutableList.copyOf(pEffects);
        this.useDuration = drinkDuration;
        this.cooldownTicks = cooldownTicks;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity){
        Player player = pLivingEntity instanceof Player ? (Player)pLivingEntity : null;
        if (player != null){
            if(!player.getAbilities().instabuild){
                if(pStack.isEmpty()){
                    return new ItemStack(Items.GLASS_BOTTLE);
                }

                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    @Override
    public Seq<TooltipComponent> getTooltips(ItemStack pStack){
        return Seq.with(new EffectsListComponent(effects, Component.translatable("tooltip.tridot.applies").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void onConsume(ItemStack pStack, Level pLevel, Player player){
        Utils.Entities.apply(player, effects);
        for(Item item : ForgeRegistries.ITEMS) {
            if(item instanceof EffectConsumableItem) {
                player.getCooldowns().addCooldown(item, cooldownTicks);
            }
        }
    }
}
