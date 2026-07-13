package pro.komaru.tridot.common.registry;

import pro.komaru.tridot.*;
import pro.komaru.tridot.api.interfaces.*;
import pro.komaru.tridot.common.registry.enchantments.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.registries.*;
import pro.komaru.tridot.common.registry.item.types.ConfiguredShield;

import java.util.function.*;

public class EnchantmentsRegistry {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Tridot.ID);
    public static final EnchantmentCategory DASH_WEAPON = EnchantmentCategory.create("radius_weapon", item -> item instanceof DashItem);
    public static final EnchantmentCategory RADIUS_WEAPON = EnchantmentCategory.create("radius_weapon", item -> item instanceof RadiusItem);
    public static final EnchantmentCategory OVERDRIVE_CATEGORY = EnchantmentCategory.create("overdrive", item -> item instanceof CooldownReductionItem);
    public static final EnchantmentCategory SHIELD_CATEGORY = EnchantmentCategory.create("shield", item -> item instanceof ConfiguredShield);

    public static final RegistryObject<Enchantment> DASH = registerEnchantment("dash", DashEnchantment::new);
    public static final RegistryObject<Enchantment> RADIUS = registerEnchantment("radius", RadiusEnchantment::new);
    public static final RegistryObject<Enchantment> OVERDRIVE = registerEnchantment("overdrive", OverdriveEnchantment::new);
    public static final RegistryObject<Enchantment> RESONANCE = registerEnchantment("resonance", MultipleParryEnchantment::new);
    public static final RegistryObject<Enchantment> VIGILANCE = registerEnchantment("vigilance", ParryWindowEnchantment::new);
    public static final RegistryObject<Enchantment> VANGUARD = registerEnchantment("vanguard", VanguardEnchantment::new);
    public static final RegistryObject<Enchantment> IRON_GRIP = registerEnchantment("iron_grip", VanguardEnchantment::new);
    public static final RegistryObject<Enchantment> DEFLECT = registerEnchantment("deflect", DeflectEnchantment::new);
    public static final RegistryObject<Enchantment> PUSH = registerEnchantment("push", DeflectEnchantment::new);

    private static RegistryObject<Enchantment> registerEnchantment(String id, Supplier<Enchantment> enchantment) {
        return ENCHANTMENTS.register(id, enchantment);
    }

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}