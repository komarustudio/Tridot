package pro.komaru.tridot.common.registry;

import pro.komaru.tridot.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.item.*;
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

    public static final TagKey<Item> BOWS = item(new ResourceLocation(Tridot.ID, "bows"));
    public static final TagKey<Item> CAN_DISABLE_SHIELD = item(new ResourceLocation(Tridot.ID, "can_disable_shield"));
    public static final TagKey<DamageType> BYPASSES_PARRY = damage(new ResourceLocation(Tridot.ID, "bypasses_parry"));
}
