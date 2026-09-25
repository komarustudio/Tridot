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

/**
 * PORT NOTE: {@code ArmorMaterial} is a final record in a registry since 1.20.5, so this class can no longer
 * <i>be</i> the material. It now describes one: {@link #toArmorMaterial()} builds the vanilla record and
 * {@link #register(DeferredRegister)} registers it under {@link #getName()} and binds the resulting holder.
 * Set/hit effect maps are keyed by the material's {@link ResourceKey} so lookups work with any holder flavour.
 */
public abstract class AbstractArmorRegistry implements TridotArmorMat{
    public AbstractArmorBuilder<?> builder;
    public static final Map<ResourceKey<ArmorMaterial>, List<ArmorEffectData>> EFFECTS = new HashMap<>();
    public static final Map<ResourceKey<ArmorMaterial>, List<HitEffectData>> HIT_EFFECTS = new HashMap<>();
    /** Every Tridot-described material, by registry key. */
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

    /** Registers the vanilla material under {@code <namespace>:<getName()>} and binds it to this registry entry. */
    public DeferredHolder<ArmorMaterial, ArmorMaterial> register(DeferredRegister<ArmorMaterial> register){
        DeferredHolder<ArmorMaterial, ArmorMaterial> holder = register.register(getName(), this::toArmorMaterial);
        bind(holder);
        return holder;
    }

    /** Binds an already-registered holder (for mods that register the material themselves). */
    public void bind(Holder<ArmorMaterial> holder){
        this.material = holder;
        ResourceKey<ArmorMaterial> key = keyOf(holder);
        MATERIALS.put(key, this);
        if(data != null) EFFECTS.put(key, data);
        if(hitData != null) HIT_EFFECTS.put(key, hitData);
    }

    /** The bound material holder; only valid after {@link #register} / {@link #bind}. */
    public Holder<ArmorMaterial> material(){
        if(material == null) throw new IllegalStateException("Armor material " + getName() + " has not been registered");
        return material;
    }

    /** Builds the vanilla record from the builder values. Layers use {@code <namespace>:<getName()>} as the asset name. */
    public ArmorMaterial toArmorMaterial(){
        EnumMap<Type, Integer> defense = new EnumMap<>(Type.class);
        for(Type type : Type.values()) defense.put(type, getDefenseForType(type));
        return new ArmorMaterial(defense, getEnchantmentValue(), getEquipSound(), this::getRepairIngredient, List.of(new ArmorMaterial.Layer(layerAsset())), getToughness(), getKnockbackResistance());
    }

    /** Asset name used for {@code textures/models/armor/<asset>_layer_N.png}. Defaults to {@code <namespace>:<name>}. */
    protected ResourceLocation layerAsset(){
        return ResourceLocation.fromNamespaceAndPath(namespace(), getName());
    }

    /** Namespace the material is registered in; used for the default layer asset. */
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