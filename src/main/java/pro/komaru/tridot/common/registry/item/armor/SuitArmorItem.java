package pro.komaru.tridot.common.registry.item.armor;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;

import java.util.*;

public class SuitArmorItem extends SkinableArmorItem{

    public SuitArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties){
        super(material, type, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags){
        super.appendHoverText(stack, context, list, flags);
        var player = Minecraft.getInstance().player;
        if(player != null){
            if(Screen.hasShiftDown()){
                list.add(Component.translatable("tooltip.tridot.equipped").withStyle(ChatFormatting.GRAY));
                list.add(getArmorSetItemComponent(player, stack, EquipmentSlot.HEAD));
                list.add(getArmorSetItemComponent(player, stack, EquipmentSlot.CHEST));
                list.add(getArmorSetItemComponent(player, stack, EquipmentSlot.LEGS));
                list.add(getArmorSetItemComponent(player, stack, EquipmentSlot.FEET));
            }else{
                list.add(Component.translatable("tooltip.tridot.shift_for_details", Component.translatable("key.keyboard.left.shift").getString()).withStyle(ChatFormatting.GRAY));
            }

            list.add(Component.empty());
        }
    }

    public static boolean sameMaterial(Holder<ArmorMaterial> a, Holder<ArmorMaterial> b){
        return a == b || a.unwrapKey().isPresent() && b.unwrapKey().isPresent() && a.unwrapKey().equals(b.unwrapKey()) || a.value() == b.value();
    }

    public static Map<EquipmentSlot, ItemStack> getFullArmorSet(Holder<ArmorMaterial> material){
        Map<EquipmentSlot, ItemStack> armorSet = new EnumMap<>(EquipmentSlot.class);
        for(var item : BuiltInRegistries.ITEM){
            if(item instanceof ArmorItem armorItem){
                if(sameMaterial(armorItem.getMaterial(), material)){
                    EquipmentSlot slot = armorItem.getEquipmentSlot();
                    armorSet.put(slot, new ItemStack(armorItem));
                }
            }
        }

        armorSet.putIfAbsent(EquipmentSlot.HEAD, ItemStack.EMPTY);
        armorSet.putIfAbsent(EquipmentSlot.CHEST, ItemStack.EMPTY);
        armorSet.putIfAbsent(EquipmentSlot.LEGS, ItemStack.EMPTY);
        armorSet.putIfAbsent(EquipmentSlot.FEET, ItemStack.EMPTY);
        return armorSet;
    }

    public boolean hasFullSuitOfArmorOn(Player player){
        ItemStack boots = player.getInventory().getArmor(0);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack chestplate = player.getInventory().getArmor(2);
        ItemStack helmet = player.getInventory().getArmor(3);

        return !helmet.isEmpty() && !chestplate.isEmpty() && !leggings.isEmpty() && !boots.isEmpty();
    }

    public static boolean hasCorrectArmorOn(Holder<ArmorMaterial> material, Player player){
        ItemStack bootsStack = player.getInventory().getArmor(0);
        ItemStack leggingsStack = player.getInventory().getArmor(1);
        ItemStack chestplateStack = player.getInventory().getArmor(2);
        ItemStack helmetStack = player.getInventory().getArmor(3);
        if(bootsStack.getItem() instanceof ArmorItem boots && leggingsStack.getItem() instanceof ArmorItem leggings && chestplateStack.getItem() instanceof ArmorItem chestplate && helmetStack.getItem() instanceof ArmorItem helmet){
            return sameMaterial(helmet.getMaterial(), material) && sameMaterial(chestplate.getMaterial(), material) && sameMaterial(leggings.getMaterial(), material) && sameMaterial(boots.getMaterial(), material);
        }

        return false;
    }

    public static boolean hasCorrectArmorOn(ResourceKey<ArmorMaterial> material, Player player){
        ItemStack bootsStack = player.getInventory().getArmor(0);
        ItemStack leggingsStack = player.getInventory().getArmor(1);
        ItemStack chestplateStack = player.getInventory().getArmor(2);
        ItemStack helmetStack = player.getInventory().getArmor(3);
        if(bootsStack.getItem() instanceof ArmorItem boots && leggingsStack.getItem() instanceof ArmorItem leggings && chestplateStack.getItem() instanceof ArmorItem chestplate && helmetStack.getItem() instanceof ArmorItem helmet){
            return helmet.getMaterial().is(material) && chestplate.getMaterial().is(material) && leggings.getMaterial().is(material) && boots.getMaterial().is(material);
        }

        return false;
    }

    public static ItemStack getArmorSetItem(ItemStack stack, EquipmentSlot slot){
        if(stack.getItem() instanceof ArmorItem armorItem){
            Map<EquipmentSlot, ItemStack> armorSet = getFullArmorSet(armorItem.getMaterial());
            return armorSet.get(slot);
        }

        return ItemStack.EMPTY;
    }

    public boolean hasArmorItem(Player player, ItemStack stack, EquipmentSlot slot){
        return player != null && player.getItemBySlot(slot).getItem() == getArmorSetItem(stack, slot).getItem();
    }

    public ChatFormatting getDisplayColor(){
        return ChatFormatting.GREEN;
    }

    public MutableComponent getArmorSetItemComponent(Player player, ItemStack stack, EquipmentSlot slot){
        return Component.literal(" ").append(Component.translatable(getArmorSetItem(stack, slot).getDescriptionId()).withStyle(Style.EMPTY.withColor(hasArmorItem(player, stack, slot) ? getDisplayColor() : ChatFormatting.RED)));
    }
}
