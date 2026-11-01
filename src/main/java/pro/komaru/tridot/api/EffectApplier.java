package pro.komaru.tridot.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public record EffectApplier(Holder<MobEffect> effect, int duration, int amplifier, float chance) {
    public static final Codec<EffectApplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(EffectApplier::effect),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(EffectApplier::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectApplier::amplifier),
            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(EffectApplier::chance)
    ).apply(instance, EffectApplier::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectApplier> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), EffectApplier::effect,
            ByteBufCodecs.VAR_INT, EffectApplier::duration,
            ByteBufCodecs.VAR_INT, EffectApplier::amplifier,
            ByteBufCodecs.FLOAT, EffectApplier::chance,
            EffectApplier::new
    );

    public static EffectApplier of(Holder<MobEffect> effect, int duration) {
        return new EffectApplier(effect, duration, 0, 1);
    }

    public static EffectApplier of(Holder<MobEffect> effect) {
        return new EffectApplier(effect, -1, 0, 1.0F);
    }
    
    public EffectApplier withAmplifier(int amplifier) {
        return new EffectApplier(this.effect, this.duration, amplifier, this.chance);
    }

    public EffectApplier withChance(float chance) {
        return new EffectApplier(this.effect, this.duration, this.amplifier, chance);
    }

    public MobEffectInstance createInstance() {
        return new MobEffectInstance(effect, duration, amplifier);
    }

    public void applyWithChance(@Nullable LivingEntity target) {
        if (target == null || this.chance <= 0.0F) return;

        if(this.chance >= 1.0F || target.getRandom().nextFloat() < chance) {
            target.addEffect(this.createInstance());
        }
    }

    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

    public boolean endsWithin(int duration) {
        return !this.isInfiniteDuration() && this.duration <= duration;
    }

    public Component formatDuration(float durationFactor) {
        if (this.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        } else {
            int i = Mth.floor((float)this.duration() * durationFactor);
            return Component.literal(StringUtil.formatTickDuration(i, 20));
        }
    }

    public Component formatDuration() {
        return this.formatDuration(1);
    }
}