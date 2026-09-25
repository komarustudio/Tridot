package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.world.level.storage.loot.LootContext.*;
import net.minecraft.world.level.storage.loot.predicates.*;

public abstract class TargetedLootCondition implements LootItemCondition{
    public EntityTarget target;
    public TargetedLootCondition(EntityTarget target) {
        this.target = target;
    }

    private static final Codec<EntityTarget> LEGACY_TARGET = Codec.STRING.comapFlatMap(name -> switch(name){
        case "killer" -> DataResult.success(EntityTarget.ATTACKER);
        case "direct_killer" -> DataResult.success(EntityTarget.DIRECT_ATTACKER);
        case "killer_player" -> DataResult.success(EntityTarget.ATTACKING_PLAYER);
        default -> DataResult.error(() -> "Unknown loot entity target: " + name);
    }, EntityTarget::getName);

    public static final Codec<EntityTarget> TARGET_CODEC = Codec.withAlternative(EntityTarget.CODEC, LEGACY_TARGET);

    /** Shared "target" field for codecs of subclasses; defaults to {@code this} like the old serializer did. */
    public static <T extends TargetedLootCondition> RecordCodecBuilder<T, EntityTarget> targetCodec(){
        return TARGET_CODEC.optionalFieldOf("target", EntityTarget.THIS).forGetter(c -> c.target == null ? EntityTarget.THIS : c.target);
    }
}
