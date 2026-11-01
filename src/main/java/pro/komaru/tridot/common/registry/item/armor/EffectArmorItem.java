package pro.komaru.tridot.common.registry.item.armor;

import net.minecraft.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.registries.*;
import pro.komaru.tridot.util.*;

import java.util.*;

public class EffectArmorItem extends SuitArmorItem{
    public EffectArmorItem(ArmorMaterial material, Type type, Properties settings){
        super(material, type, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flags){
        super.appendHoverText(stack, world, list, flags);
        if (stack.getItem() instanceof ArmorItem armor) {
            var effects = AbstractArmorRegistry.EFFECTS.get(armor.getMaterial());
            if (effects != null) {
                var component = Component.translatable("tooltip.tridot.applies_fullkit").withStyle(ChatFormatting.GRAY);
                for (int i = 0; i < effects.size(); i++) {
                    MobEffect effect = effects.get(i).applier().effect();
                    var effectName = effect.getDisplayName().getString();
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