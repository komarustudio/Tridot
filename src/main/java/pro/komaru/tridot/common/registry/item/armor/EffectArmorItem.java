package pro.komaru.tridot.common.registry.item.armor;

import net.minecraft.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;

import java.util.*;

public class EffectArmorItem extends SuitArmorItem{
    public EffectArmorItem(Holder<ArmorMaterial> material, Type type, Properties settings){
        super(material, type, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags){
        super.appendHoverText(stack, context, list, flags);
        if (stack.getItem() instanceof ArmorItem armor) {
            var effects = AbstractArmorRegistry.effectsFor(armor.getMaterial());
            if (effects != null) {
                var component = Component.translatable("tooltip.tridot.applies_fullkit").withStyle(ChatFormatting.GRAY);
                for (int i = 0; i < effects.size(); i++) {
                    Holder<MobEffect> effect = effects.get(i).instance().get().getEffect();
                    var effectName = effect.value().getDisplayName().getString();
                    component.append(Component.literal(effectName).withStyle(stack.getRarity().getStyleModifier()));
                    if (i < effects.size() - 1) {
                        component.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    }
                }

                list.add(component);
            }
        }
    }
}