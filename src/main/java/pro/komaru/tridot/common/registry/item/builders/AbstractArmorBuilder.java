package pro.komaru.tridot.common.registry.item.builders;

import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

import java.util.*;
import java.util.function.*;

//example: public static final ArmorRegistry COBALT = new ArmorRegistry.Builder("cobalt").protection(18).mul(46).enchantValue(18).knockbackRes(0.05f).ingredient(() -> Ingredient.of(ItemsRegistry.cobaltIngot.get())).build();
// PORT NOTE: attributes, mob effects and equip sounds are Holders in 1.21 (Attributes.*, DeferredHolder and
// SoundEvents.* all qualify), so the Supplier<Attribute>/Supplier<MobEffect>/SoundEvent parameters became Holders.
public abstract class AbstractArmorBuilder<T>{
    public String name;
    public float headPercent = 20;
    public float chestPercent = 35;
    public float leggingsPercent = 25;
    public float bootsPercent = 20;
    public float headProtectionAmount;
    public float chestplateProtectionAmount;
    public float leggingsProtectionAmount;
    public float bootsProtectionAmount;

    public float toughness;
    public float knockbackResistance;

    public int enchantmentValue;
    public int[] durability = {11, 16, 16, 13};
    public int durabilityMultiplier;

    public Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
    public Supplier<Ingredient> repairIngredient;
    public List<ArmorEffectData> data;
    public List<HitEffectData> hitData;

    public float headAtrPercent = 20;
    public float chestAtrPercent = 35;
    public float leggingsAtrPercent = 25;
    public float bootsAtrPercent = 20;
    public Multimap<Holder<Attribute>, AttributeData> attributeMap = HashMultimap.create();

    public AbstractArmorBuilder(String name){
        this.name = name;
    }

    /**
     * Represents an effect that can be applied by wearing a specific armor set.
     * @param instance Supplier for the MobEffectInstance to apply.
     * @param condition Predicate determining when the effect should apply.
     */
    public record ArmorEffectData(Supplier<MobEffectInstance> instance, Predicate<Player> condition) {
        public static final Predicate<Player> ALWAYS_TRUE = player -> true;
        public static final int INFINITE_DURATION = -1;
        public static ArmorEffectData createData(Holder<MobEffect> effect) {
            return new ArmorEffectData(() -> new MobEffectInstance(effect, INFINITE_DURATION), ALWAYS_TRUE);
        }

        public static ArmorEffectData createData(Supplier<MobEffectInstance> instance, Predicate<Player> condition) {
            return new ArmorEffectData(instance, condition);
        }
    }

    public record HitEffectData(Supplier<MobEffectInstance> instance, Predicate<Player> condition, float chance) {
        public static final Predicate<Player> ALWAYS_TRUE = player -> true;
        public static HitEffectData createData(Holder<MobEffect> effect) {
            return new HitEffectData(() -> new MobEffectInstance(effect, 120), ALWAYS_TRUE, 1);
        }

        public static HitEffectData createData(Supplier<MobEffectInstance> instance, Predicate<Player> condition, float chance) {
            return new HitEffectData(instance, condition, chance);
        }

        public static HitEffectData createData(Supplier<MobEffectInstance> instance, Predicate<Player> condition) {
            return new HitEffectData(instance, condition, 1);
        }
    }

    public record AttributeData(float value, Operation operation){}
    public AbstractArmorBuilder<T> addAttrs(Multimap<Holder<Attribute>, AttributeData> map) {
        attributeMap.putAll(map);
        return this;
    }

    public AbstractArmorBuilder<T> setAttrs(Multimap<Holder<Attribute>, AttributeData> map){
        attributeMap = map;
        return this;
    }

    public AbstractArmorBuilder<T> addAttr(Holder<Attribute> attribute, AttributeData mod) {
        attributeMap.put(attribute, mod);
        return this;
    }

    public AbstractArmorBuilder<T> hitEffects(List<HitEffectData> data) {
        this.hitData = data;
        return this;
    }

    public AbstractArmorBuilder<T> effects(List<ArmorEffectData> data) {
        this.data = data;
        return this;
    }

    public AbstractArmorBuilder<T> mul(int durabilityMul){
        this.durabilityMultiplier = durabilityMul;
        return this;
    }

    public AbstractArmorBuilder<T> durability(int head, int chest, int leggings, int boots) {
        this.durability = new int[]{head, chest, leggings, boots};
        return this;
    }

    public AbstractArmorBuilder<T> protection(float percent) {
        float head = (percent * headPercent) / 100;
        float chest = (percent * chestPercent) / 100;
        float leggings = (percent * leggingsPercent) / 100;
        float boots = (percent * bootsPercent) / 100;
        float remainder = percent - (head + chest + leggings + boots);
        chest += remainder;

        this.headProtectionAmount = head;
        this.chestplateProtectionAmount = chest;
        this.leggingsProtectionAmount = leggings;
        this.bootsProtectionAmount = boots;
        return this;
    }

    /**
     * Attribute percent distribution between parts
     * @apiNote Default: 20, 35, 25, 20
     */
    public AbstractArmorBuilder<T> atrDistribution(float head, float chest, float leggings, float boots) {
        this.headAtrPercent = head;
        this.chestAtrPercent = chest;
        this.leggingsAtrPercent = leggings;
        this.bootsAtrPercent = boots;
        return this;
    }

    /**
     * Armor percent distribution between parts
     * @apiNote Default: 20, 35, 25, 20
     */
    public AbstractArmorBuilder<T> distribution(float head, float chest, float leggings, float boots) {
        this.headPercent = head;
        this.chestPercent = chest;
        this.leggingsPercent = leggings;
        this.bootsPercent = boots;
        return this;
    }

    public AbstractArmorBuilder<T> protection(float head, float chest, float leggings, float boots) {
        this.headProtectionAmount = head;
        this.chestplateProtectionAmount = chest;
        this.leggingsProtectionAmount = leggings;
        this.bootsProtectionAmount = boots;
        return this;
    }

    public AbstractArmorBuilder<T> enchantValue(int value){
        this.enchantmentValue = value;
        return this;
    }

    public AbstractArmorBuilder<T> sound(Holder<SoundEvent> sound){
        this.equipSound = sound;
        return this;
    }

    public AbstractArmorBuilder<T> sound(SoundEvent sound){
        this.equipSound = Holder.direct(sound);
        return this;
    }

    public AbstractArmorBuilder<T> toughness(float value){
        this.toughness = value;
        return this;
    }

    public AbstractArmorBuilder<T> knockbackRes(float value){
        this.knockbackResistance = value;
        return this;
    }

    public AbstractArmorBuilder<T> ingredient(Supplier<Ingredient> value){
        this.repairIngredient = value;
        return this;
    }

    public abstract T build();
}