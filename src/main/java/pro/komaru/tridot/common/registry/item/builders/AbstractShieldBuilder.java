package pro.komaru.tridot.common.registry.item.builders;

import com.google.common.collect.ImmutableList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
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
    public float blockedPercent = 100;
    public int useDuration;
    public int cooldownTicks = 45;
    public int parryWindow = 10;
    public boolean canParry = true;

    public float chance = 1;
    public ImmutableList<MobEffectInstance> onParryEffects = ImmutableList.of();

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
     * @param chance   Chance of applying effects to target
     * @param pEffects Effects that will be applied to target
     */
    public AbstractShieldBuilder<T> setEffects(float chance, MobEffectInstance... pEffects){
        this.chance = chance;
        this.onParryEffects = ImmutableList.copyOf(pEffects);
        return this;
    }

    /**
     * @param pEffects Effects that will be applied to target
     */
    public AbstractShieldBuilder<T> setEffects(MobEffectInstance... pEffects){
        this.onParryEffects = ImmutableList.copyOf(pEffects);
        return this;
    }

    public AbstractShieldBuilder<T> setUseDuration(int useTime){
        this.useDuration = useTime;
        this.infiniteUse = false;
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
