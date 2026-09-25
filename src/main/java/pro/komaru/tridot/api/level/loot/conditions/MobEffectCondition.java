package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootContext.*;
import net.minecraft.world.level.storage.loot.predicates.*;

public class MobEffectCondition extends TargetedLootCondition{
    public static final MapCodec<MobEffectCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        TargetedLootCondition.targetCodec(),
        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(c -> c.effect)
    ).apply(inst, MobEffectCondition::new));

    private final Holder<MobEffect> effect;
    public MobEffectCondition(EntityTarget target, Holder<MobEffect> effect){
        super(target);
        this.effect = effect;
    }

    @Override
    public boolean test(LootContext lootContext){
        if(lootContext.getParamOrNull(target.getParam()) instanceof LivingEntity livingEntity){
            return livingEntity.hasEffect(effect);
        }
        return false;
    }

    @Override
    public LootItemConditionType getType(){
        return LootConditionsRegistry.MOB_EFFECT_CONDITION.get();
    }
}
