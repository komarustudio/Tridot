package pro.komaru.tridot.common.registry.item.skins;

import net.minecraft.client.resources.model.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.client.event.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.client.model.item.*;
import pro.komaru.tridot.client.model.render.item.*;
import pro.komaru.tridot.util.struct.data.*;

import java.util.*;

public class SkinRegistryManager {
    private static final Map<String, ItemSkin> skins = new HashMap<>();
    private static final Seq<ItemSkin> skinList = Seq.with();
    private static final SkinRegistryManager INSTANCE = new SkinRegistryManager();

    public static SkinRegistryManager getInstance() {
        return INSTANCE;
    }

    public static void add(ItemSkin skin) {
        if (skin == null || skin.skinBuilder == null || skin.skinBuilder.id == null) {
            return;
        }

        skins.put(skin.skinBuilder.id, skin);
        skinList.add(skin);
    }

    public static ItemSkin get(int index) {
        if (index >= 0 && index < skinList.size) {
            return skinList.get(index);
        }

        return null;
    }

    public static ItemSkin get(String id) {
        return skins.get(id);
    }

    public static int size() {
        return skins.size();
    }

    public static Seq<ItemSkin> getApplicableSkins(ItemStack stack) {
        Seq<ItemSkin> skins = Seq.with();
        getSkins().forEach(s -> {
            if(s.appliesOn(stack)) skins.add(s);
        });

        return skins;
    }

    public static Seq<ItemSkin> getSkins() {
        return Seq.with(skinList);
    }

    public static Map<String, ItemSkin> getSkinsMap() {
        return Collections.unmodifiableMap(skins);
    }

    public void registerSkinProvider(ISkinProvider provider) {
        provider.initializeSkins();
        if(FMLEnvironment.dist.isClient()){
            provider.registerModels();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void addSkinModel(Map<ModelResourceLocation, BakedModel> map, ResourceLocation id){
        BakedModel model = map.get(ModelResourceLocation.inventory(id));
        CustomModel newModel = new CustomModel(model, new ItemSkinItemOverrides());
        map.replace(ModelResourceLocation.inventory(id), newModel);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addLargeModel(Map<ModelResourceLocation, BakedModel> map, String modId, String skin){
        LargeItemRenderer.bakeModel(map, modId, "skin/" + skin);
        ItemSkinModels.addModelSkins(modId + ":" + skin, map.get(SkinRegistryManager.getModelLocationSkin(modId + ":" + skin)));
    }

    public static ModelResourceLocation getModelLocationSkin(String id){
        int i = id.indexOf(":");
        String modId = id.substring(0, i);
        String skinId = id.substring(i + 1);
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(modId, "item/skin/" + skinId));
    }

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistryEvents{
        @SubscribeEvent
        public static void modelRegistrySkins(ModelEvent.RegisterAdditional event){
            for(String skin : ItemSkinModels.getSkins()){
                event.register(getModelLocationSkin(skin));
            }
        }

        @SubscribeEvent
        public static void modelBakeSkins(ModelEvent.ModifyBakingResult event){
            Map<ModelResourceLocation, BakedModel> map = event.getModels();

            for(String skin : ItemSkinModels.getSkins()){
                BakedModel model = map.get(getModelLocationSkin(skin));
                ItemSkinModels.addModelSkins(skin, model);
            }
        }
    }
}
