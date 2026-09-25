package pro.komaru.tridot.mixin.client;

import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import pro.komaru.tridot.client.tooltip.*;

import java.util.*;

@Mixin(value = AttributeUtil.class, remap = false)
public class AttributeUtilMixin{

    @ModifyVariable(method = "applyTextFor", at = @At("HEAD"), argsOnly = true)
    private static Multimap<Holder<Attribute>, AttributeModifier> tridot$modifyTooltipModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifierMap, ItemStack stack, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> ignored, AttributeTooltipContext ctx){
        List<AttributeTooltipModifier> tooltipModifiers = TooltipModifierHandler.getModifiers();
        if(tooltipModifiers.isEmpty() || modifierMap == null || modifierMap.isEmpty()) return modifierMap;

        Player player = ctx.player();
        TooltipFlag flag = ctx.flag();
        Multimap<Holder<Attribute>, AttributeModifier> copied = modifierMap instanceof SortedSetMultimap<?, ?> ? AttributeUtil.sortedMap() : LinkedHashMultimap.create();
        for(Map.Entry<Holder<Attribute>, AttributeModifier> entry : modifierMap.entries()){
            Holder<Attribute> key = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            if(key == null || modifier == null) continue;

            double amount = modifier.amount();
            AttributeModifier.Operation operation = modifier.operation();
            boolean changed = false;
            boolean base = false;
            for(AttributeTooltipModifier tooltipModifier : tooltipModifiers){
                if(tooltipModifier.isModifiable(key, modifier, player, flag)){
                    AttributeTooltipModifier.ModifyResult result = tooltipModifier.modify(modifier, amount, operation);
                    modifier = result.getModifier();
                    amount = result.getAmount();
                    operation = result.getOperation();
                    changed = true;
                }

                if(tooltipModifier.isToolBase(modifier, player, flag)){
                    base = true;
                }

                if(changed || base) break;
            }

            ResourceLocation id = modifier.id();
            if(base){
                ResourceLocation baseId = key.value().getBaseId();
                if(baseId != null) id = baseId;
            }

            if(changed || !id.equals(modifier.id())){
                copied.put(key, new AttributeModifier(id, amount, operation));
            }else{
                copied.put(key, modifier);
            }
        }

        return copied;
    }
}
