package pro.komaru.tridot.common.registry.item.components;

import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.inventory.tooltip.*;

public record AbilityComponent(MutableComponent component, ResourceLocation icon, int paddingTop, int textPaddingTop, int iconSize) implements TooltipComponent{
    public AbilityComponent(MutableComponent component, ResourceLocation icon){
        this(component, icon, 0, 0, 18);
    }

    public AbilityComponent(MutableComponent component, ResourceLocation icon, int paddingTop){
        this(component, icon, paddingTop, 0, 18);
    }

    public AbilityComponent(MutableComponent component, ResourceLocation icon, int paddingTop, int textPaddingTop){
        this(component, icon, paddingTop, textPaddingTop, 18);
    }
}