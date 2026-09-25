package pro.komaru.tridot.api.level.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.loot.*;

import javax.annotation.*;

public class AddLootModifier extends LootModifier{
    public static final MapCodec<AddLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(m -> m.lootTable),
    Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter((m) -> m.chance))).apply(inst, AddLootModifier::new));

    private final ResourceLocation lootTable;
    private final float chance;

    public AddLootModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable, float chance) {
        super(conditionsIn);
        this.lootTable = lootTable;
        this.chance = chance;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if(context.getRandom().nextFloat() <= chance){
            LootTable extraLoot = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable));
            extraLoot.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}