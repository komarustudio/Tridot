package pro.komaru.tridot.common.registry.item;

import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import pro.komaru.tridot.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.registries.*;

public class AttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Tridot.ID);
    public static final DeferredHolder<Attribute, Attribute> PROJECTILE_DAMAGE = ATTRIBUTES.register("projectile_damage", () -> new RangedAttribute("attribute.tridot.projectile_damage", 0.0D, 0.0D, 1024.0D){
        @Override
        public ResourceLocation getBaseId(){
            return Tridot.BASE_PROJECTILE_DAMAGE_ID;
        }
    }.setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PERCENT_ARMOR = ATTRIBUTES.register("percent_armor", () -> new RangedAttribute("attribute.tridot.percent_armor", 0.0D, 0.0D, 100.0D).setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}