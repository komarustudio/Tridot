package pro.komaru.tridot.client.tooltip;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;

public class TooltipModifierHandler{
    public static int attributeTooltipSize = 0;
    public static List<AttributeTooltipModifier> modifiers = new ArrayList<>();

    public static void register(AttributeTooltipModifier modifier){
        modifiers.add(modifier);
    }

    public static void add(ResourceLocation id) {
        register(new AttributeTooltipModifier(){
            public boolean isToolBase(AttributeModifier modifier, Player player, TooltipFlag flag){
                return modifier.id().equals(id);
            }
        });
    }

    public static List<AttributeTooltipModifier> getModifiers(){
        return modifiers;
    }

    public static int getAttributeTooltipSize(){
        return attributeTooltipSize;
    }
}
