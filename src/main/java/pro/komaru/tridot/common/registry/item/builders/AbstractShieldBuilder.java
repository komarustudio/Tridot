package pro.komaru.tridot.common.registry.item.builders;

import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import pro.komaru.tridot.api.EffectApplier;
import pro.komaru.tridot.common.registry.item.types.*;

import javax.annotation.*;
import java.util.*;

public abstract class AbstractShieldBuilder<T extends ConfiguredShield>{
    public Tier tier = Tiers.WOOD;
    public Properties itemProperties;
    public Multimap<Holder<Attribute>, AttributeModifier> extraAttributes = LinkedHashMultimap.create();
    public Map<Holder<Attribute>, EquipmentSlotGroup> attributeSlots = Maps.newHashMap();
    public boolean hasCustomAttributes = false;
    @Nullable public SoundEvent parrySound = SoundEvents.SHIELD_BREAK;

    /**
     * This sound will be played on the block if the blockPercent is less than 100
     */
    @Nullable public SoundEvent blockSound = SoundEvents.SHIELD_BLOCK;

    public boolean infiniteUse = true;
    public boolean canParry = true;

    public float blockedPercent = 1;
    public float returnedDamagePercent = 0;
    public int useDuration;
    public int cooldownTicks = 45;
    public int parryCooldownTicks = 100;
    public int shieldDisableTicks = 200;
    public int parryWindow = 12;
    public int onShieldReleaseTicks = 32;

    public ImmutableList<EffectApplier> defenderBlockEffects = ImmutableList.of();
    public ImmutableList<EffectApplier> defenderParryEffects = ImmutableList.of();

    public ImmutableList<EffectApplier> attackerParryEffects = ImmutableList.of();
    public ImmutableList<EffectApplier> attackerBlockEffects = ImmutableList.of();

    public ImmutableList<EffectApplier> attackerShieldDisableEffects = ImmutableList.of();
    public ImmutableList<EffectApplier> defenderShieldDisableEffects = ImmutableList.of();

    public AbstractShieldBuilder(Properties itemProperties){
        this.itemProperties = itemProperties;
    }

    public AbstractShieldBuilder(float defPercent, Properties itemProperties){
        this.blockedPercent = defPercent;
        this.itemProperties = itemProperties;
    }

    public AbstractShieldBuilder<T> addAttribute(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slotGroup){
        this.extraAttributes.put(attribute, modifier);
        this.attributeSlots.put(attribute, slotGroup);
        this.hasCustomAttributes = true;
        return this;
    }

    public AbstractShieldBuilder<T> addAttribute(Holder<Attribute> attribute, AttributeModifier modifier){
        return addAttribute(attribute, modifier, EquipmentSlotGroup.OFFHAND);
    }

    public Properties buildProperties(){
        if(hasCustomAttributes){
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            this.extraAttributes.forEach((attribute, modifier) -> {
                EquipmentSlotGroup group = this.attributeSlots.getOrDefault(attribute, EquipmentSlotGroup.OFFHAND);
                builder.add(attribute, modifier, group);
            });

            return this.itemProperties.attributes(builder.build());
        }

        return this.itemProperties;
    }

    public AbstractShieldBuilder<T> setTier(Tier tier){
        this.tier = tier;
        return this;
    }

    public AbstractShieldBuilder<T> setCanParry(boolean val){
        canParry = val;
        return this;
    }

    /**
     * @param event Sound that will be played when parry is performed
     */
    public AbstractShieldBuilder<T> setParrySound(SoundEvent event){
        this.parrySound = event;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Defender on shield disable
     */
    public AbstractShieldBuilder<T> addDefenderShieldDisableEffects(EffectApplier... effects){
        this.defenderShieldDisableEffects = ImmutableList.copyOf(effects);
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on shield disable
     */
    public AbstractShieldBuilder<T> addAttackerShieldDisableEffects(EffectApplier... effects){
        this.attackerShieldDisableEffects = ImmutableList.copyOf(effects);
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Defender on block
     */
    public AbstractShieldBuilder<T> addDefenderBlockEffects(EffectApplier... effects){
        this.defenderBlockEffects = ImmutableList.copyOf(effects);
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Defender on parry
     */
    public AbstractShieldBuilder<T> addDefenderParryEffects(EffectApplier... effects){
        this.defenderParryEffects = ImmutableList.copyOf(effects);
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on block
     */
    public AbstractShieldBuilder<T> addAttackerBlockEffects(EffectApplier... effects){
        this.attackerBlockEffects = ImmutableList.copyOf(effects);
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on parry
     */
    public AbstractShieldBuilder<T> addAttackerParryEffects(EffectApplier... effects){
        this.attackerParryEffects = ImmutableList.copyOf(effects);
        return this;
    }

    public AbstractShieldBuilder<T> setReturnedPercent(float percent){
        this.returnedDamagePercent = percent;
        return this;
    }

    public AbstractShieldBuilder<T> setUseDuration(int useTime){
        this.useDuration = useTime;
        this.infiniteUse = false;
        return this;
    }

    public AbstractShieldBuilder<T> setDisabledCooldownTime(int cooldownTime){
        this.shieldDisableTicks = cooldownTime;
        return this;
    }

    public AbstractShieldBuilder<T> setParryCooldownTime(int cooldownTime){
        this.parryCooldownTicks = cooldownTime;
        return this;
    }

    public AbstractShieldBuilder<T> setCooldownTime(int cooldownTime){
        this.cooldownTicks = cooldownTime;
        return this;
    }

    public AbstractShieldBuilder<T> setParryWindow(int window){
        this.parryWindow = window;
        return this;
    }

    public AbstractShieldBuilder<T> setonShieldReleaseTicks(int ticks) {
        this.onShieldReleaseTicks = ticks;
        return this;
    }

    /**
     * @return Build of ConfiguredShield with all the configurations you set :p
     */
    public abstract T build();
}
