package pro.komaru.tridot.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record EffectApplier(Supplier<MobEffect> effectSupplier, int duration, int amplifier, float chance) {
    public static final Codec<EffectApplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(EffectApplier::effect),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(EffectApplier::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectApplier::amplifier),
            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(EffectApplier::chance)
    ).apply(instance, (effect, duration, amplifier, chance) -> new EffectApplier(() -> effect, duration, amplifier, chance)));

    public EffectApplier(MobEffect effect, int duration, int amplifier, float chance) {
        this(() -> effect, duration, amplifier, chance);
    }

    public EffectApplier(MobEffect effect, int duration, float chance) {
        this(() -> effect, duration, 0, chance);
    }

    public EffectApplier(MobEffect effect, int duration, int amplifier) {
        this(() -> effect, duration, amplifier, 1.0F);
    }

    public EffectApplier(MobEffect effect, int duration) {
        this(() -> effect, duration, 0, 1.0F);
    }

    public EffectApplier(MobEffect effect) {
        this(() -> effect, -1, 0, 1.0F);
    }

    public EffectApplier(Supplier<MobEffect> effectSupplier, int duration, float chance) {
        this(effectSupplier, duration, 0, chance);
    }

    public EffectApplier(Supplier<MobEffect> effectSupplier, int duration, int amplifier) {
        this(effectSupplier, duration, amplifier, 1.0F);
    }

    public EffectApplier(Supplier<MobEffect> effectSupplier, int duration) {
        this(effectSupplier, duration, 0, 1.0F);
    }

    public EffectApplier(Supplier<MobEffect> effectSupplier) {
        this(effectSupplier, -1, 0, 1.0F);
    }

    public static EffectApplier of(MobEffect effect, int duration) {
        return new EffectApplier(() -> effect, duration, 0, 1.0F);
    }

    public static EffectApplier of(MobEffect effect) {
        return new EffectApplier(() -> effect, -1, 0, 1.0F);
    }

    public static EffectApplier of(Supplier<MobEffect> effect, int duration) {
        return new EffectApplier(effect, duration, 0, 1.0F);
    }

    public static EffectApplier of(Supplier<MobEffect> effect) {
        return new EffectApplier(effect, -1, 0, 1.0F);
    }

    public MobEffect effect() {
        return this.effectSupplier.get();
    }

    public EffectApplier withAmplifier(int amplifier) {
        return new EffectApplier(this.effectSupplier, this.duration, amplifier, this.chance);
    }

    public EffectApplier withChance(float chance) {
        return new EffectApplier(this.effectSupplier, this.duration, this.amplifier, chance);
    }

    public MobEffectInstance createInstance() {
        return new MobEffectInstance(this.effect(), this.duration, this.amplifier);
    }

    public void applyWithChance(@Nullable LivingEntity target) {
        if (target == null || this.chance <= 0.0F) return;

        if (this.chance >= 1.0F || target.getRandom().nextFloat() < this.chance) {
            target.addEffect(this.createInstance());
        }
    }

    public void apply(@Nullable LivingEntity target) {
        if (target != null) {
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
            int i = Mth.floor((float)this.duration * durationFactor);
            return Component.literal(StringUtil.formatTickDuration(i));
        }
    }

    public Component formatDuration() {
        return this.formatDuration(1.0F);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeRegistryId(ForgeRegistries.MOB_EFFECTS, effect());
        buffer.writeVarInt(duration);
        buffer.writeVarInt(amplifier);
        buffer.writeFloat(chance);
    }

    public static EffectApplier fromNetwork(FriendlyByteBuf buffer) {
        MobEffect effect = buffer.readRegistryId();
        int duration = buffer.readVarInt();
        int amplifier = buffer.readVarInt();
        float chance = buffer.readFloat();
        return new EffectApplier(effect, duration, amplifier, chance);
    }
}
