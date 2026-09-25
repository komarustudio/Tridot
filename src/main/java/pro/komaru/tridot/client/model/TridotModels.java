package pro.komaru.tridot.client.model;

import pro.komaru.tridot.*;
import pro.komaru.tridot.client.model.armor.*;
import pro.komaru.tridot.client.model.book.*;
import pro.komaru.tridot.client.model.item.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;

import java.util.*;

public class TridotModels{
    public static final ModelLayerLocation EMPTY_ARMOR_LAYER = addLayer("empty_armor");
    public static final ModelLayerLocation BOOK_LAYER = addLayer("book");

    public static CustomBookModel BOOK = null;
    public static EmptyArmorModel EMPTY_ARMOR = null;

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistryEvents{
        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
            event.registerLayerDefinition(EMPTY_ARMOR_LAYER, EmptyArmorModel::createBodyLayer);
            event.registerLayerDefinition(BOOK_LAYER, CustomBookModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event){
            EMPTY_ARMOR = new EmptyArmorModel(event.getEntityModels().bakeLayer(EMPTY_ARMOR_LAYER));
            BOOK = new CustomBookModel(event.getEntityModels().bakeLayer(BOOK_LAYER));
        }
    }

    public static ModelLayerLocation addLayer(String layer){
        return addLayer(Tridot.ID, layer);
    }

    public static ModelLayerLocation addLayer(String modId, String layer){
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(modId, layer), "main");
    }

    public static ModelResourceLocation addCustomModel(String modId, String model){
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(modId, model));
    }

    public static ModelResourceLocation inventory(ResourceLocation item){
        return ModelResourceLocation.inventory(item);
    }

    public static ModelResourceLocation inventory(ResourceLocation item, String suffix){
        return ModelResourceLocation.inventory(item.withSuffix(suffix));
    }

    public static ModelResourceLocation sideLoaded(ResourceLocation item){
        return ModelResourceLocation.standalone(item.withPrefix("item/"));
    }

    public static ModelResourceLocation sideLoaded(ResourceLocation item, String suffix){
        return sideLoaded(item.withSuffix(suffix));
    }

    public static void addCustomRenderItemModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation item){
        BakedModel model = map.get(inventory(item));
        CustomModel customModel = new CustomRenderModel(model, new CustomItemOverrides());
        map.replace(inventory(item), customModel);
    }

    public static void addCrossbowItemModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation item, CrossbowItemOverrides itemOverrides) {
        BakedModel model = map.get(inventory(item));
        CustomModel customModel = new CustomModel(model, itemOverrides);

        for (int i = 0; i < 3; i++) {
            BakedModel pullModel = map.get(sideLoaded(item, "_pulling_" + i));
            itemOverrides.pullingModels.add(pullModel);
        }
        itemOverrides.arrowModel = map.get(sideLoaded(item, "_arrow"));
        itemOverrides.fireworkModel = map.get(sideLoaded(item, "_firework"));

        map.replace(inventory(item), customModel);
    }

    public static void addCrossbowItemModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation item) {
        addCrossbowItemModel(map, item, new CrossbowItemOverrides());
    }
    public static ArrayList<ModelResourceLocation> getCrossbowModels(String modId, String item) {
        ArrayList<ModelResourceLocation> models = new ArrayList<>();
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_0")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_1")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_2")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_arrow")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_firework")));
        return models;
    }

    public static void addCrossbowItemModel(ModelEvent.RegisterAdditional event, String modId, String item) {
        for (ModelResourceLocation model : getCrossbowModels(modId, item)) {
            event.register(model);
        }
    }

    public static void addBowItemModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation item, BowItemOverrides itemOverrides){
        BakedModel model = map.get(inventory(item));
        CustomModel customModel = new CustomModel(model, itemOverrides);

        for(int i = 0; i < 3; i++){
            BakedModel pullModel = map.get(sideLoaded(item, "_pulling_" + i));
            itemOverrides.models.add(pullModel);
        }

        map.replace(inventory(item), customModel);
    }

    public static void addBowItemModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation item){
        addBowItemModel(map, item, new BowSkinItemOverrides());
    }

    public static ArrayList<ModelResourceLocation> getBowModels(String modId, String item){
        ArrayList<ModelResourceLocation> models = new ArrayList<>();
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_0")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_1")));
        models.add(sideLoaded(ResourceLocation.fromNamespaceAndPath(modId, item + "_pulling_2")));
        return models;
    }

    public static void addBowItemModel(ModelEvent.RegisterAdditional event, String modId, String item){
        for(ModelResourceLocation model : getBowModels(modId, item)){
            event.register(model);
        }
    }
}
