package pro.komaru.tridot.common.registry;

import pro.komaru.tridot.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;

public class TagsRegistry{
    public static TagKey<Item> item(final ResourceLocation name){
        return TagKey.create(Registries.ITEM, name);
    }

    public static TagKey<Block> block(final ResourceLocation name){
        return TagKey.create(Registries.BLOCK, name);
    }

    public static TagKey<EntityType<?>> entity(final ResourceLocation name){
        return TagKey.create(Registries.ENTITY_TYPE, name);
    }

    public static TagKey<DamageType> damage(final ResourceLocation name){
        return TagKey.create(Registries.DAMAGE_TYPE, name);
    }

    public static TagKey<PaintingVariant> painting(final ResourceLocation name){
        return TagKey.create(Registries.PAINTING_VARIANT, name);
    }

    public static TagKey<Enchantment> enchantment(final ResourceLocation name){
        return TagKey.create(Registries.ENCHANTMENT, name);
    }

    public static final TagKey<Item> BOWS = item(Tridot.ofTridot("bows"));
    public static final TagKey<Item> CAN_DISABLE_SHIELD = item(Tridot.ofTridot("can_disable_shield"));
    public static final TagKey<DamageType> BYPASSES_PARRY = damage(Tridot.ofTridot("bypasses_parry"));

    /** Items that implement {@code DashItem}; makes {@code tridot:dash} applicable. */
    public static final TagKey<Item> ENCHANTABLE_DASH_WEAPON = EnchantmentsRegistry.DASH_WEAPON;
    /** Items that implement {@code RadiusItem}; makes {@code tridot:radius} applicable. */
    public static final TagKey<Item> ENCHANTABLE_RADIUS_WEAPON = EnchantmentsRegistry.RADIUS_WEAPON;
    /** Items that implement {@code CooldownReductionItem}; makes {@code tridot:overdrive} applicable. */
    public static final TagKey<Item> ENCHANTABLE_OVERDRIVE = EnchantmentsRegistry.OVERDRIVE_CATEGORY;
    /** {@code ConfiguredShield} items; makes the shield enchantments applicable. */
    public static final TagKey<Item> ENCHANTABLE_SHIELD = EnchantmentsRegistry.SHIELD_CATEGORY;
}
