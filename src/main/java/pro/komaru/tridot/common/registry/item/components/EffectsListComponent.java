package pro.komaru.tridot.common.registry.item.components;

import net.minecraft.network.chat.*;
import net.minecraft.world.inventory.tooltip.*;
import pro.komaru.tridot.api.EffectApplier;

import javax.annotation.*;
import java.util.*;

public record EffectsListComponent(List<EffectApplier> list, @Nullable Component component) implements TooltipComponent{
}