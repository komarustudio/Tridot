package pro.komaru.tridot.common.registry.item.builders;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectList {
    private float chance = 1;
    private final ImmutableList<MobEffectInstance> effects;
    public static EffectList EMPTY = new EffectList(ImmutableList.of());

    public EffectList(float chance, ImmutableList<MobEffectInstance> effects) {
        this.chance = chance;
        this.effects = effects;
    }

    public EffectList(ImmutableList<MobEffectInstance> effects) {
        this.effects = effects;
    }

    public EffectList(float chance, MobEffectInstance... pEffects) {
        this.chance = chance;
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public EffectList(MobEffectInstance... pEffects) {
        this.effects = ImmutableList.copyOf(pEffects);
    }

    public ImmutableList<MobEffectInstance> getEffects() {
        return effects;
    }

    public float getChance() {
        return chance;
    }
}