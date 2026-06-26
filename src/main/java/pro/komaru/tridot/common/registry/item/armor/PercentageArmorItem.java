package pro.komaru.tridot.common.registry.item.armor;

import com.google.common.collect.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.common.config.CommonConfig;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ItemStack.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraftforge.api.distmarker.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.builders.*;

import java.text.*;
import java.util.*;


public class PercentageArmorItem extends ArmorItem{
    public ArmorMaterial material;
    public UUID uuid;
    private final float defense;
    private final float toughness;
    protected final float knockbackResistance;
    public static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266744_) -> {
        p_266744_.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        p_266744_.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        p_266744_.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        p_266744_.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });

    public final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (p_41704_) -> p_41704_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public Multimap<Attribute, AttributeModifier> defaultModifiers;

    public PercentageArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties){
        super(pMaterial, pType, pProperties);
        this.material = pMaterial;
        this.toughness = pMaterial.getToughness();
        if(pMaterial instanceof TridotArmorMat mat) {
            this.defense = mat.getPercentDefenseForType(pType);
        } else this.defense = pMaterial.getDefenseForType(pType);

        this.knockbackResistance = pMaterial.getKnockbackResistance();
        this.uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(pType);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        if (this.knockbackResistance > 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", this.knockbackResistance, AttributeModifier.Operation.ADDITION));
        }

        builder.put(AttributeRegistry.PERCENT_ARMOR.get(), new AttributeModifier(uuid, "PercentArmor", this.defense, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public int getDefense() {
        return Math.round(this.defense);
    }

    public float getToughness() {
        return toughness;
    }

    public float getTotalDefense(ArmorMaterial material) {
        if(material instanceof TridotArmorMat tridotArmorMat) {
            return tridotArmorMat.getPercentDefenseForType(Type.HELMET) + tridotArmorMat.getPercentDefenseForType(Type.CHESTPLATE) + tridotArmorMat.getPercentDefenseForType(Type.LEGGINGS) + tridotArmorMat.getPercentDefenseForType(Type.BOOTS);
        }

        return material.getDefenseForType(Type.HELMET) + material.getDefenseForType(Type.CHESTPLATE) + material.getDefenseForType(Type.LEGGINGS) + material.getDefenseForType(Type.BOOTS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced){
        if(CommonConfig.PERCENT_ARMOR.get() != null && CommonConfig.PERCENT_ARMOR.get()){
            pTooltipComponents.add(Component.translatable("tooltip.tridot.total_armor", getTotalDefense(((PercentageArmorItem)pStack.getItem()).getMaterial()) + "%").withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    public float attrDist(AbstractArmorBuilder<?> builder, EquipmentSlot pEquipmentSlot, float percent) {
        float head = (percent * builder.headAtrPercent) / 100;
        float chest = (percent * builder.chestAtrPercent) / 100;
        float leggings = (percent * builder.leggingsAtrPercent) / 100;
        float boots = (percent * builder.bootsAtrPercent) / 100;
        float remainder = percent - (head + chest + leggings + boots);
        chest += remainder;
        return switch(pEquipmentSlot) {
            case HEAD ->  head;
            case CHEST -> chest;
            case LEGS -> leggings;
            case FEET -> boots;
            default -> 0;
        };
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot){
        if(pEquipmentSlot == this.type.getSlot()){
            ImmutableMultimap.Builder<Attribute, AttributeModifier> m = ImmutableMultimap.builder();
            m.putAll(getModifiedMultimap());
            if(this.getMaterial() instanceof TridotArmorMat armorRegistry){
                armorRegistry.builder().attributeMap.forEach((attrSupplier, data) -> {
                    AttributeModifier modifier1 = new AttributeModifier(uuid, "Attribute Modifier", attrDist(armorRegistry.builder(), pEquipmentSlot, data.value()), data.operation());
                    m.put(attrSupplier.get(), modifier1);
                });
            }

            return m.build();
        }

        return super.getDefaultAttributeModifiers(pEquipmentSlot);
    }

    private @NotNull Multimap<Attribute, AttributeModifier> getModifiedMultimap(){
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        if(!CommonConfig.PERCENT_ARMOR.get()){
            map.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", this.defense, AttributeModifier.Operation.ADDITION));
            map.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
            if(this.knockbackResistance > 0){
                map.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", this.knockbackResistance, AttributeModifier.Operation.ADDITION));
            }
        } else {
            map = this.defaultModifiers;
        }

        return map;
    }
}
