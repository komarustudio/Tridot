package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import pro.komaru.tridot.api.level.event.*;

public record EventActiveCondition(ResourceLocation eventId) implements LootItemCondition{
    public static final MapCodec<EventActiveCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ResourceLocation.CODEC.fieldOf("event").forGetter(EventActiveCondition::eventId)
    ).apply(inst, EventActiveCondition::new));

    @Override
    public boolean test(LootContext context) {
        ServerLevel server = context.getLevel();
        if (server != null) {
            return GameplayEventManager.get(server).isEventActive(this.eventId);
        }

        return false;
    }

    @Override
    public LootItemConditionType getType() {
        return LootConditionsRegistry.ACTIVE_EVENT_CONDITION.get();
    }
}
