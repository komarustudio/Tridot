package pro.komaru.tridot.api.interfaces;

import net.minecraft.world.item.*;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;

public interface CooldownReductionItem{
    default int getCooldownReduction(int cooldown, ItemStack stack) {
        var level = stack.getEnchantmentLevel(EnchantmentsRegistry.OVERDRIVE.get());
        float factor = level >= 5 ? 0.015f : 0.02f;

        return Math.max(0, Math.round(cooldown * factor * level));
    }
}
