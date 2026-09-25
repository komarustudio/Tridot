package pro.komaru.tridot.api.level.loot.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.*;

import javax.annotation.Nullable;
import java.time.*;

public class LocalDateCondition implements LootItemCondition{
    public static final MapCodec<LocalDateCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        IntRange.CODEC.fieldOf("day_of_month").forGetter(c -> c.dayOfMonth),
        IntRange.CODEC.fieldOf("month").forGetter(c -> c.month)
    ).apply(inst, LocalDateCondition::new));

    public final IntRange dayOfMonth;
    public final IntRange month;

    LocalDateCondition(IntRange day, IntRange month){
        this.dayOfMonth = day;
        this.month = month;
    }

    @NotNull
    public LootItemConditionType getType(){
        return LootConditionsRegistry.LOCAL_DATE_CONDITION.get();
    }

    public boolean test(LootContext lootContext){
        LocalDate localdate = LocalDate.now();
        int day = localdate.getDayOfMonth();
        int month = localdate.getMonth().getValue();
        return this.dayOfMonth.test(lootContext, day) && this.month.test(lootContext, month);
    }

    public static Builder time(IntRange pDay, IntRange pMonth){
        return new Builder(pDay, pMonth);
    }

    public static class Builder implements LootItemCondition.Builder{
        @Nullable
        private final IntRange day;
        private final IntRange month;

        public Builder(IntRange day, IntRange month){
            this.day = day;
            this.month = month;
        }

        public LocalDateCondition build(){
            return new LocalDateCondition(this.day, this.month);
        }
    }
}
