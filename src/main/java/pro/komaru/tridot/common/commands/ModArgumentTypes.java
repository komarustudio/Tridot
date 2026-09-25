package pro.komaru.tridot.common.commands;

import net.minecraft.commands.synchronization.*;
import net.minecraft.core.registries.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.common.commands.arguments.*;

public class ModArgumentTypes{
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARG_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Tridot.ID);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<?, ?>> SKIN_ARG = ARG_TYPES.register("skin", () -> ArgumentTypeInfos.registerByClass(ItemSkinArgumentType.class, SingletonArgumentInfo.contextFree(ItemSkinArgumentType::skinArgument)));

    public static void register(IEventBus eventBus){
        ARG_TYPES.register(eventBus);
    }
}
