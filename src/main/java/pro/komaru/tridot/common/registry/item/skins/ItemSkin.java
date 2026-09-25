package pro.komaru.tridot.common.registry.item.skins;

import net.minecraft.client.model.*;
import net.minecraft.client.player.*;
import net.minecraft.client.resources.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.client.gfx.text.DotStyle;
import pro.komaru.tridot.client.model.TridotModels;
import pro.komaru.tridot.client.model.armor.ArmorModel;
import pro.komaru.tridot.common.registry.TridotDataComponents;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.struct.Structs;
import pro.komaru.tridot.util.struct.data.Seq;

import javax.annotation.*;
import java.util.*;

public class ItemSkin{
    public Seq<SkinEntry> entries = Seq.with();
    public static Col loreCol = Col.fromHex("F9D281");
    public SkinBuilder skinBuilder;

    public ItemSkin(SkinBuilder skinBuilder) {
        this.skinBuilder = skinBuilder;
    }

    public void setupSkinEntries(){}

    public boolean appliesOn(ItemStack stack) {
        return entries.contains(e -> e.appliesOn(stack));
    }

    public ItemStack apply(ItemStack stack) {
        stack.set(TridotDataComponents.SKIN, skinBuilder.id);
        return stack;
    }

    /** Removes any skin from the stack (replaces {@code tag.remove("skin")}). */
    public static ItemStack remove(ItemStack stack) {
        stack.remove(TridotDataComponents.SKIN);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.contains("skin")) {
            stack.set(DataComponents.CUSTOM_DATA, data.update(tag -> tag.remove("skin")));
        }

        return stack;
    }

    @Nullable
    public Component getHoverName(){
        return skinBuilder.hoverName;
    }

    @Nullable
    public List<MutableComponent> getComponents(){
        return skinBuilder.component;
    }

    @Nullable
    public static ItemSkin itemSkin(ItemStack stack) {
        String id = stack.get(TridotDataComponents.SKIN);
        if (id == null) {
            // Stacks upgraded from 1.20.1 carry the old root tag inside custom_data.
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            id = data != null ? data.copyTag().getString("skin") : "";
        }
        return SkinRegistryManager.get(id);
    }

    public Seq<SkinEntry> skinEntries(){
        return entries;
    }

    public ResourceLocation id() {
        return ResourceLocation.parse(skinBuilder.id);
    }

    public Col color() {
        return skinBuilder.color;
    }

    public String translatedName(){
        return Component.translatable("item_skin." + id().toLanguageKey()).getString();
    }

    public String translatedLoreName(){
        return Component.translatable("item_skin." + id().toLanguageKey()+".lore").getString();
    }

    public Component skinName(){
        return Component.translatable(translatedName()).setStyle(DotStyle.of().color(color()));
    }

    public Component skinComponent(){
        return Component.translatable("lore.tridot.skin").setStyle(DotStyle.of().color(loreCol)).append(" ").append(skinName());
    }

    @OnlyIn(Dist.CLIENT)
    public ArmorModel getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> _default){
        return Structs.safeGet(entries.find(e -> e.appliesOn(stack)), e -> e.armorModel(entity,stack,slot,_default), () -> TridotModels.EMPTY_ARMOR);
    }

    @OnlyIn(Dist.CLIENT)
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type){
        return Structs.safeGet(entries.find(e -> e.appliesOn(stack)), e -> e.armorTexture(entity,stack,slot,type), () -> Tridot.ID + ":textures/models/armor/skin/empty.png");
    }

    @OnlyIn(Dist.CLIENT)
    public String getItemModelName(ItemStack stack){
        return Structs.safeGet(entries.find(e -> e.appliesOn(stack)), e -> e.itemModel(stack));
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean defaultModel(Entity entity){
        if(entity instanceof AbstractClientPlayer player){
            return player.getSkin().model() == PlayerSkin.Model.WIDE;
        }

        return true;
    }
}
