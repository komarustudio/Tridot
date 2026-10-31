package pro.komaru.tridot.client.gfx.postprocess.bloom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;
import pro.komaru.tridot.Tridot;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Item bloom.
 * <p>
 * Supports two ways of defining item bloom:
 * <ul>
 *   <li>Inline in any item model JSON via {@code "tridot_bloom": "<namespace>:item/<texture>"} inside {@code "textures"}
 *       (works inside {@code neoforge:separate_transforms} / {@code forge:separate_transforms} perspectives and {@code base} as well).</li>
 *   <li>Standalone {@code assets/<id>/models/item/<item>_bloom.json} model file.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BloomItemModels {
    private static final Map<Item, ModelResourceLocation> LOCATIONS = new HashMap<>();
    private static volatile Map<Item, BakedModel> baked = new IdentityHashMap<>();
    private static final Map<BakedModel, BakedModel> MODEL_BLOOM = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile boolean hasInlineBlooms = false;

    private BloomItemModels() {}

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        LOCATIONS.clear();
        MODEL_BLOOM.clear();
        hasInlineBlooms = false;
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            ResourceLocation model = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath() + "_bloom");
            ResourceLocation file = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "models/" + model.getPath() + ".json");
            if (resources.getResource(file).isPresent()) {
                ModelResourceLocation mrl = ModelResourceLocation.standalone(model);
                LOCATIONS.put(entry.getValue(), mrl);
                event.register(mrl);
            }
        }

        if (!LOCATIONS.isEmpty()) {
            Tridot.LOGGER.info("[Bloom] Found {} standalone item bloom model(s)", LOCATIONS.size());
        }
    }

    @SubscribeEvent
    public static void onBaked(ModelEvent.BakingCompleted event) {
        Map<Item, BakedModel> result = new IdentityHashMap<>();
        BakedModel missing = event.getModelManager().getMissingModel();
        LOCATIONS.forEach((item, location) -> {
            BakedModel model = event.getModels().get(location);
            if (model != null && model != missing) result.put(item, model);
        });

        baked = result;
        if (!MODEL_BLOOM.isEmpty()) {
            Tridot.LOGGER.info("[Bloom] Baked {} inline tridot_bloom model(s)", MODEL_BLOOM.size());
        }
    }

    public static void putModelBloom(BakedModel parent, BakedModel bloom) {
        if (parent != null && bloom != null) {
            MODEL_BLOOM.put(parent, bloom);
            hasInlineBlooms = true;
        }
    }

    public static boolean hasInlineBlooms() {
        return hasInlineBlooms;
    }

    @Nullable
    public static BakedModel getForModel(BakedModel model) {
        return MODEL_BLOOM.get(model);
    }

    @Nullable
    public static BakedModel get(Item item) {
        return baked.get(item);
    }
}
