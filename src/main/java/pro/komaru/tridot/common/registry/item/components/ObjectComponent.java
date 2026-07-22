package pro.komaru.tridot.common.registry.item.components;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

public record ObjectComponent(MutableComponent component, Item icon, int paddingTop, int textPaddingTop) implements TooltipComponent{
    public ObjectComponent(MutableComponent text, Item icon){
        this(text, icon, 0, 0);
    }

    public ObjectComponent(MutableComponent component, Item icon, int paddingTop){
        this(component, icon, paddingTop, 0);
    }
}