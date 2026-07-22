package pro.komaru.tridot.api.interfaces;

import net.minecraft.world.item.*;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;

public interface CooldownReductionItem{
    default int getCooldownReduction(int cooldown, ItemStack stack) {
        int level = stack.getEnchantmentLevel(EnchantmentsRegistry.OVERDRIVE.get());
        float modifier = 1.0f;
        if (level > 0) {
            modifier = 0.95f;
            if (level > 1) modifier -= (level - 1) * 0.02f;
        }

        return Math.max(0, Math.round(cooldown * modifier));
    }
}
