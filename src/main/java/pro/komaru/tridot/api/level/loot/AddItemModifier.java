package pro.komaru.tridot.api.level.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.loot.*;

import javax.annotation.*;

public class AddItemModifier extends LootModifier{
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(BuiltInRegistries.ITEM.byNameCodec()
    .fieldOf("item").forGetter((m) -> m.item),
    Codec.INT.optionalFieldOf("count", 1).forGetter((m) -> m.count),
    Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter((m) -> m.chance)
    )).apply(inst, AddItemModifier::new));

    private final Item item;
    private final int count;
    private final float chance;

    public AddItemModifier(LootItemCondition[] conditionsIn, Item item, int count, float chance){
        super(conditionsIn);
        this.item = item;
        this.count = count;
        this.chance = chance;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context){
        if(context.getRandom().nextFloat() <= chance){
            ItemStack addedStack = new ItemStack(item, count);

            if(addedStack.getCount() < addedStack.getMaxStackSize()){
                generatedLoot.add(addedStack);
            }else{
                int i = addedStack.getCount();

                while(i > 0){
                    ItemStack subStack = addedStack.copy();
                    subStack.setCount(Math.min(addedStack.getMaxStackSize(), i));
                    i -= subStack.getCount();
                    generatedLoot.add(subStack);
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec(){
        return CODEC;
    }
}
