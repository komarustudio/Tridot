package pro.komaru.tridot.common.registry.item.skins.entries;

import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.common.registry.item.skins.*;
import pro.komaru.tridot.util.struct.func.Boolf;

public class FilterSkinEntry implements SkinEntry{
    public String model;
    public Boolf<ItemStack> filter;

    public FilterSkinEntry(String model, Boolf<ItemStack> filter){
        this.filter = filter;
        this.model = model;
    }

    public boolean appliesOn(ItemStack stack){
        return filter.get(stack);
    }

    @OnlyIn(Dist.CLIENT)
    public String itemModel(ItemStack stack){
        return this.model;
    }
}