package pro.komaru.tridot.common.registry.item.armor;

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.common.config.CommonConfig;
import net.minecraft.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.builders.*;

import java.text.*;
import java.util.*;
import java.util.function.Supplier;


public class PercentageArmorItem extends ArmorItem{
    public Holder<ArmorMaterial> material;
    public ResourceLocation id;
    private final float defense;
    private final float toughness;
    protected final float knockbackResistance;
    public static final EnumMap<ArmorItem.Type, ResourceLocation> ARMOR_MODIFIER_ID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        for(ArmorItem.Type type : ArmorItem.Type.values()) map.put(type, ResourceLocation.withDefaultNamespace("armor." + type.getName()));
    });

    public final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (p_41704_) -> p_41704_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public ItemAttributeModifiers defaultModifiers;
    private final Supplier<ItemAttributeModifiers> percentModifiers = Suppliers.memoize(() -> buildModifiers(true));
    private final Supplier<ItemAttributeModifiers> vanillaModifiers = Suppliers.memoize(() -> buildModifiers(false));

    public PercentageArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties){
        super(pMaterial, pType, pProperties);
        this.material = pMaterial;
        this.toughness = pMaterial.value().toughness();
        TridotArmorMat mat = AbstractArmorRegistry.tridotMaterial(pMaterial);
        if(mat != null) {
            this.defense = mat.getPercentDefenseForType(pType);
        } else this.defense = pMaterial.value().getDefense(pType);

        this.knockbackResistance = pMaterial.value().knockbackResistance();
        this.id = ARMOR_MODIFIER_ID_PER_TYPE.get(pType);
        this.defaultModifiers = percentModifiers.get();
    }

    public int getDefense() {
        return Math.round(this.defense);
    }

    public float getToughness() {
        return toughness;
    }

    public float getTotalDefense(Holder<ArmorMaterial> material) {
        TridotArmorMat tridotArmorMat = AbstractArmorRegistry.tridotMaterial(material);
        if(tridotArmorMat != null) {
            return tridotArmorMat.getPercentDefenseForType(Type.HELMET) + tridotArmorMat.getPercentDefenseForType(Type.CHESTPLATE) + tridotArmorMat.getPercentDefenseForType(Type.LEGGINGS) + tridotArmorMat.getPercentDefenseForType(Type.BOOTS);
        }

        ArmorMaterial value = material.value();
        return value.getDefense(Type.HELMET) + value.getDefense(Type.CHESTPLATE) + value.getDefense(Type.LEGGINGS) + value.getDefense(Type.BOOTS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced){
        if(CommonConfig.PERCENT_ARMOR.get() != null && CommonConfig.PERCENT_ARMOR.get()){
            pTooltipComponents.add(Component.translatable("tooltip.tridot.total_armor", getTotalDefense(((PercentageArmorItem)pStack.getItem()).getMaterial()) + "%").withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
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

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(){
        return CommonConfig.PERCENT_ARMOR.get() ? percentModifiers.get() : vanillaModifiers.get();
    }

    private ItemAttributeModifiers buildModifiers(boolean percent){
        EquipmentSlot slot = this.type.getSlot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
        ItemAttributeModifiers.Builder m = ItemAttributeModifiers.builder();
        if(percent){
            if (this.knockbackResistance > 0) {
                m.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE), group);
            }

            m.add(AttributeRegistry.PERCENT_ARMOR, new AttributeModifier(id, this.defense, AttributeModifier.Operation.ADD_VALUE), group);
            m.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, this.toughness, AttributeModifier.Operation.ADD_VALUE), group);
        }else{
            m.add(Attributes.ARMOR, new AttributeModifier(id, this.defense, AttributeModifier.Operation.ADD_VALUE), group);
            m.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, this.toughness, AttributeModifier.Operation.ADD_VALUE), group);
            if(this.knockbackResistance > 0){
                m.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE), group);
            }
        }

        TridotArmorMat armorRegistry = AbstractArmorRegistry.tridotMaterial(this.getMaterial());
        if(armorRegistry != null){
            armorRegistry.builder().attributeMap.forEach((attribute, data) -> {
                AttributeModifier modifier1 = new AttributeModifier(id, attrDist(armorRegistry.builder(), slot, data.value()), data.operation());
                m.add(attribute, modifier1, group);
            });
        }

        return m.build();
    }
}
