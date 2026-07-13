package pro.komaru.tridot.common.registry.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;

public class DeflectEnchantment extends Enchantment {

    public DeflectEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentsRegistry.SHIELD_CATEGORY, new EquipmentSlot[]{EquipmentSlot.OFFHAND});
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean checkCompatibility(Enchantment pEnchantment) {
        return super.checkCompatibility(pEnchantment);
    }
}