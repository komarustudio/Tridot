package pro.komaru.tridot.common.registry.item.skins.entries;

import net.minecraft.resources.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.*;
import pro.komaru.tridot.common.registry.item.skins.*;

public class ItemExtendingSkinEntry implements SkinEntry{
    public final Class<? extends Item> item;
    public String model;

    /**
     * @param modelLoc location, ex. Tridot.ofTridot("my_skin");
     */
    public ItemExtendingSkinEntry(Class<? extends Item> item, ResourceLocation modelLoc){
        this.item = item;
        this.model = modelLoc.toString();
    }

    @Override
    public boolean appliesOn(ItemStack itemStack){
        return item.isInstance(itemStack.getItem());
    }

    @OnlyIn(Dist.CLIENT)
    public String itemModel(ItemStack stack){
        return model;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String armorTexture(Entity entity, ItemStack stack, EquipmentSlot slot, String type) {
        return model;
    }
}
