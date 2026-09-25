package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.*;

public class DifficultyCondition implements LootItemCondition{
    public static final MapCodec<DifficultyCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Difficulty.CODEC.fieldOf("difficulty").forGetter(c -> c.difficulty),
        Codec.BOOL.optionalFieldOf("exact_match", false).forGetter(c -> c.exactMatch)
    ).apply(inst, DifficultyCondition::new));

    public final Difficulty difficulty;
    public final boolean exactMatch;

    DifficultyCondition(Difficulty difficulty, boolean exactMatch){
        this.difficulty = difficulty;
        this.exactMatch = exactMatch;
    }

    @NotNull
    public LootItemConditionType getType(){
        return LootConditionsRegistry.DIFFICULTY_CONDITION.get();
    }

    public boolean test(LootContext lootContext){
        Level level = lootContext.getLevel();
        var currentID = level.getDifficulty().getId();
        var targetID = this.difficulty.getId();
        return exactMatch ? targetID == currentID : targetID >= currentID;
    }

    public static Builder difficulty(Difficulty difficulty, boolean exactMatch){
        return new Builder(difficulty, exactMatch);
    }

    public static class Builder implements LootItemCondition.Builder{
        private final Difficulty difficulty;
        public final boolean exactMatch;

        public Builder(Difficulty difficulty, boolean exactMatch){
            this.difficulty = difficulty;
            this.exactMatch = exactMatch;
        }

        public DifficultyCondition build(){
            return new DifficultyCondition(this.difficulty, exactMatch);
        }
    }
}
