package pro.komaru.tridot.client.render.gui.particle;

import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.neoforged.fml.event.lifecycle.*;
import net.minecraft.core.registries.*;

import java.util.*;

public class ParticleEmitterHandler {
    public static final Map<Item, List<IGUIParticleItem>> EMITTERS = new HashMap<>();

    public static void registerEmitters(FMLClientSetupEvent event) {
        for(Item item : BuiltInRegistries.ITEM) {
            if(item instanceof IGUIParticleItem supplier) {
                registerEmitters(item, supplier);
            }
        }
    }

    public static void registerEmitters(Item item, IGUIParticleItem emitter) {
        if (EMITTERS.containsKey(item)) {
            EMITTERS.get(item).add(emitter);
        } else {
            EMITTERS.put(item, new ArrayList<>(List.of(emitter)));
        }
    }

    public static void registerEmitters(IGUIParticleItem emitter, Item... items) {
        for (Item item : items) {
            registerEmitters(item, emitter);
        }
    }

    public interface IGUIParticleItem{
        default void spawnParticlesEarly(ScreenParticleHolder target, Level level, float partialTick, ItemStack stack, float x, float y) {
        }

        default void spawnParticlesLate(ScreenParticleHolder target, Level level, float partialTick, ItemStack stack, float x, float y) {
        }
    }
}