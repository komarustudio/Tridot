package pro.komaru.tridot.common.registry;

import com.mojang.serialization.*;
import net.minecraft.core.component.*;
import net.minecraft.network.codec.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.*;

public class TridotDataComponents{
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Tridot.ID);

    /** Id of the {@code ItemSkin} applied to the stack ({@code "namespace:skin_id"}). Replaces the {@code "skin"} NBT string. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SKIN = COMPONENTS.registerComponentType("skin",
        builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static void register(IEventBus eventBus){
        COMPONENTS.register(eventBus);
    }
}
