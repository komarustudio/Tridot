package pro.komaru.tridot.api.level.loot.conditions;

import net.minecraft.core.registries.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.*;

public class LootConditionsRegistry{
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Tridot.ID);
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> LOCAL_DATE_CONDITION = LOOT_CONDITION_TYPES.register("local_date", () -> new LootItemConditionType(LocalDateCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> DIFFICULTY_CONDITION = LOOT_CONDITION_TYPES.register("difficulty", () -> new LootItemConditionType(DifficultyCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> MOB_CATEGORY_CONDITION = LOOT_CONDITION_TYPES.register("mob_category", () -> new LootItemConditionType(MobCategoryCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> MOB_EFFECT_CONDITION = LOOT_CONDITION_TYPES.register("mob_effect", () -> new LootItemConditionType(MobEffectCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> ACTIVE_EVENT_CONDITION = LOOT_CONDITION_TYPES.register("active_event", () -> new LootItemConditionType(EventActiveCondition.CODEC));

    public static void init(IEventBus bus){
        LOOT_CONDITION_TYPES.register(bus);
    }
}
