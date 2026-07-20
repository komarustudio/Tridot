package pro.komaru.tridot.common.registry.item.builders;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import pro.komaru.tridot.common.registry.item.types.ConfiguredShield;

import javax.annotation.Nullable;

public abstract class AbstractShieldBuilder<T extends ConfiguredShield>{
    public Tier tier = Tiers.WOOD;
    public Properties itemProperties;
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
    public int parryCooldownTicks = 20;
    public int shieldDisableTicks = 100;
    public int parryWindow = 10;

    public EffectList defenderBlockEffects = EffectList.EMPTY;
    public EffectList defenderParryEffects = EffectList.EMPTY;

    public EffectList attackerParryEffects = EffectList.EMPTY;
    public EffectList attackerBlockEffects = EffectList.EMPTY;

    public EffectList attackerShieldDisableEffects = EffectList.EMPTY;
    public EffectList defenderShieldDisableEffects = EffectList.EMPTY;

    public AbstractShieldBuilder(Properties itemProperties){
        this.itemProperties = itemProperties;
    }

    public AbstractShieldBuilder(float defPercent, Properties itemProperties){
        this.blockedPercent = defPercent;
        this.itemProperties = itemProperties;
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
    public AbstractShieldBuilder<T> addDefenderShieldDisableEffects(EffectList effects){
        this.defenderShieldDisableEffects = effects;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on shield disable
     */
    public AbstractShieldBuilder<T> addAttackerShieldDisableEffects(EffectList effects){
        this.attackerShieldDisableEffects = effects;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Defender on block
     */
    public AbstractShieldBuilder<T> addDefenderBlockEffects(EffectList effects){
        this.defenderBlockEffects = effects;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Defender on parry
     */
    public AbstractShieldBuilder<T> addDefenderParryEffects(EffectList effects){
        this.defenderParryEffects = effects;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on block
     */
    public AbstractShieldBuilder<T> addAttackerBlockEffects(EffectList effects){
        this.attackerBlockEffects = effects;
        return this;
    }

    /**
     * @param effects Effect list that will be applied to Attacker on parry
     */
    public AbstractShieldBuilder<T> addAttackerParryEffects(EffectList effects){
        this.attackerParryEffects = effects;
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

    /**
     * @return Build of ConfiguredShield with all the configurations you set :p
     */
    public abstract T build();
}
