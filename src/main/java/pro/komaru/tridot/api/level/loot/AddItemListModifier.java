package pro.komaru.tridot.api.level.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.*;
import net.minecraft.core.registries.*;
import net.minecraft.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.loot.*;

import javax.annotation.*;
import java.util.*;

public class AddItemListModifier extends LootModifier{
    public static final MapCodec<AddItemListModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(ExtraCodecs.nonEmptyList(BuiltInRegistries.ITEM.byNameCodec().listOf()).fieldOf("items").forGetter(m -> m.items),
    Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter((m) -> m.chance))).apply(inst, AddItemListModifier::new));

    private final List<Item> items;
    private final float chance;

    protected AddItemListModifier(LootItemCondition[] conditions, List<Item> items, float chance){
        super(conditions);
        this.items = items;
        this.chance = chance;
    }

    @Nonnull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context){
        if(context.getRandom().nextFloat() <= chance){
            ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
            for(LootItemCondition condition : this.conditions){
                if(!condition.test(context)){
                    return generatedLoot;
                }
            }

            items.forEach(item -> generatedLoot.add(item.getDefaultInstance()));
            newLoot.add(Util.getRandom(generatedLoot, context.getRandom()));
            return newLoot;
        }

        return generatedLoot;
    }

    public MapCodec<? extends IGlobalLootModifier> codec(){
        return CODEC;
    }
}
