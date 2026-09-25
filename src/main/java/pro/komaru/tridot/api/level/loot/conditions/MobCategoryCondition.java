package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.level.storage.loot.predicates.*;

public class MobCategoryCondition implements LootItemCondition{
    public static final MapCodec<MobCategoryCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        MobCategory.CODEC.fieldOf("mob_category").forGetter(c -> c.category)
    ).apply(inst, MobCategoryCondition::new));

    private final MobCategory category;

    public MobCategoryCondition(MobCategory category){
        this.category = category;
    }

    @Override
    public boolean test(LootContext lootContext){
        if(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity livingEntity){
            return livingEntity.getType().getCategory() == this.category;
        }
        return false;
    }

    @Override
    public LootItemConditionType getType(){
        return LootConditionsRegistry.MOB_CATEGORY_CONDITION.get();
    }
}
