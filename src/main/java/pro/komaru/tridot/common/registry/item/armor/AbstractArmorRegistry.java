package pro.komaru.tridot.common.registry.item.armor;

import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ArmorItem.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.common.registry.item.builders.*;
import pro.komaru.tridot.common.registry.item.builders.AbstractArmorBuilder.*;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AbstractArmorRegistry implements TridotArmorMat{
    public AbstractArmorBuilder<?> builder;
    public static final Map<ResourceKey<ArmorMaterial>, List<ArmorEffectData>> EFFECTS = new HashMap<>();
    public static final Map<ResourceKey<ArmorMaterial>, List<HitEffectData>> HIT_EFFECTS = new HashMap<>();
    public static final Map<ResourceKey<ArmorMaterial>, TridotArmorMat> MATERIALS = new HashMap<>();

    private final List<ArmorEffectData> data;
    private final List<HitEffectData> hitData;
    @Nullable
    private Holder<ArmorMaterial> material;

    public AbstractArmorRegistry(AbstractArmorBuilder<?> builder, List<ArmorEffectData> data, List<HitEffectData> hitData){
        this.builder = builder;
        this.data = data;
        this.hitData = hitData;
    }

    public DeferredHolder<ArmorMaterial, ArmorMaterial> register(DeferredRegister<ArmorMaterial> register){
        DeferredHolder<ArmorMaterial, ArmorMaterial> holder = register.register(getName(), this::toArmorMaterial);
        bind(holder);
        return holder;
    }

    public void bind(Holder<ArmorMaterial> holder){
        this.material = holder;
        ResourceKey<ArmorMaterial> key = keyOf(holder);
        MATERIALS.put(key, this);
        if(data != null) EFFECTS.put(key, data);
        if(hitData != null) HIT_EFFECTS.put(key, hitData);
    }

    public Holder<ArmorMaterial> material(){
        if(material == null) throw new IllegalStateException("Armor material " + getName() + " has not been registered");
        return material;
    }

    public ArmorMaterial toArmorMaterial(){
        EnumMap<Type, Integer> defense = new EnumMap<>(Type.class);
        for(Type type : Type.values()) defense.put(type, getDefenseForType(type));
        return new ArmorMaterial(defense, getEnchantmentValue(), getEquipSound(), this::getRepairIngredient, List.of(new ArmorMaterial.Layer(layerAsset())), getToughness(), getKnockbackResistance());
    }

    protected ResourceLocation layerAsset(){
        return ResourceLocation.fromNamespaceAndPath(namespace(), getName());
    }

    protected abstract String namespace();

    public static ResourceKey<ArmorMaterial> keyOf(Holder<ArmorMaterial> holder){
        return holder.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Armor material holder is not registered: " + holder));
    }

    @Nullable
    public static TridotArmorMat tridotMaterial(Holder<ArmorMaterial> holder){
        return holder.unwrapKey().map(MATERIALS::get).orElse(null);
    }

    @Nullable
    public static List<ArmorEffectData> effectsFor(Holder<ArmorMaterial> holder){
        return holder.unwrapKey().map(EFFECTS::get).orElse(null);
    }

    @Nullable
    public static List<HitEffectData> hitEffectsFor(Holder<ArmorMaterial> holder){
        return holder.unwrapKey().map(HIT_EFFECTS::get).orElse(null);
    }

    @Override
    public AbstractArmorBuilder<?> builder(){
        return builder;
    }

    public int getDurabilityForType(Type pType){
        return builder.durability[pType.ordinal()] * builder.durabilityMultiplier;
    }

    public float getPercentDefenseForType(Type pType){
        return switch(pType){
            case HELMET -> builder.headProtectionAmount;
            case CHESTPLATE -> builder.chestplateProtectionAmount;
            case LEGGINGS -> builder.leggingsProtectionAmount;
            case BOOTS -> builder.bootsProtectionAmount;
            case BODY -> 0;
        };
    }

    public int getDefenseForType(Type pType){
        return switch(pType){
            case HELMET -> (int)builder.headProtectionAmount;
            case CHESTPLATE -> (int)builder.chestplateProtectionAmount;
            case LEGGINGS -> (int)builder.leggingsProtectionAmount;
            case BOOTS -> (int)builder.bootsProtectionAmount;
            case BODY -> 0;
        };
    }

    public int getEnchantmentValue(){
        return builder.enchantmentValue;
    }

    @NotNull
    public Holder<SoundEvent> getEquipSound(){
        return builder.equipSound;
    }

    @NotNull
    public Ingredient getRepairIngredient(){
        return builder.repairIngredient.get();
    }

    @NotNull
    public abstract String getName();

    public float getToughness(){
        return builder.toughness;
    }

    public float getKnockbackResistance(){
        return builder.knockbackResistance;
    }
}