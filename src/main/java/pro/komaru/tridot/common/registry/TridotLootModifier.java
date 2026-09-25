package pro.komaru.tridot.common.registry;

import com.mojang.serialization.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.level.loot.*;

public class TridotLootModifier{
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Tridot.ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemModifier>> ADD_ITEM = LOOT_MODIFIERS.register("add_item", () -> AddItemModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddItemListModifier>> ADD_ITEM_LIST = LOOT_MODIFIERS.register("add_item_list", () -> AddItemListModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddLootModifier>> ADD_LOOT = LOOT_MODIFIERS.register("add_loot", () -> AddLootModifier.CODEC);

    public static void register(IEventBus eventBus){
        LOOT_MODIFIERS.register(eventBus);
    }
}
