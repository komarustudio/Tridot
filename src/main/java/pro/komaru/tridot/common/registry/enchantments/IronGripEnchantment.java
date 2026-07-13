package pro.komaru.tridot.common.registry.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;

public class IronGripEnchantment extends Enchantment {

    public IronGripEnchantment() {
        super(Rarity.RARE, EnchantmentsRegistry.SHIELD_CATEGORY, new EquipmentSlot[]{EquipmentSlot.OFFHAND});
    }

    public int getMaxLevel() {
        return 3;
    }

    public boolean checkCompatibility(Enchantment pEnchantment) {
        return super.checkCompatibility(pEnchantment);
    }
}