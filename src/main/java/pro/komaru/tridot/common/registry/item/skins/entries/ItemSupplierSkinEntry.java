package pro.komaru.tridot.common.registry.item.skins.entries;

import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.common.registry.item.skins.*;

import java.util.function.*;

public class ItemSupplierSkinEntry implements SkinEntry{
    public final Supplier<Item> item;
    public String skin;

    public ItemSupplierSkinEntry(Supplier<Item> item, ResourceLocation modelLoc){
        this.item = item;
        this.skin = modelLoc.toString();
    }

    public boolean appliesOn(ItemStack itemStack){
        return itemStack.is(item.get());
    }

    @OnlyIn(Dist.CLIENT)
    public String itemModel(ItemStack stack){
        return this.skin;
    }
}