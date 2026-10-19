package pro.komaru.tridot.api.fx;

import net.minecraft.core.particles.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;

import java.util.*;
import java.util.function.*;

/**
 * Execution context container passed to {@link Effect.EffectRenderer}.
 * Closely mirrors Mindustry's EffectContainer with fin(), fout(), fslope(), and vector helpers.
 */
public class EffectContainer {
    public Effect effect;
    public Level level;
    public double x, y, z;
    public float rotation; // yaw in degrees
    public float pitch;    // pitch in degrees
    public Col color = Col.white;
    public Object data;
    public int id;
    public float time;
    public float partialTicks;
    public float lifetime;
    private static final Random RAND = new Random();

    @FunctionalInterface
    public interface BiConsumerDouble {
        void accept(double x, double z);
    }

    @FunctionalInterface
    public interface TriConsumerDouble {
        void accept(double x, double y, double z);
    }

    public float fin() {
        return lifetime <= 0.0F ? 1.0F : Mth.clamp((time + partialTicks) / lifetime, 0.0F, 1.0F);
    }

    public float fin(Interp interp) {
        return interp.apply(fin());
    }

    public float fout() {
        return 1.0F - fin();
    }

    public float fout(Interp interp) {
        return interp.apply(fout());
    }

    public float fslope() {
        return (0.5F - Math.abs(fin() - 0.5F)) * 2.0F;
    }

    public float finpow() {
        return fin() * fin();
    }

    public float foutpow() {
        return fout() * fout();
    }

    public float finpow(float power) {
        return (float) Math.pow(fin(), power);
    }

    public float foutpow(float power) {
        return (float) Math.pow(fout(), power);
    }

    public boolean isFirstTick() {
        return time <= 0.0F;
    }

    public boolean isLastTick() {
        return time >= lifetime - 1.0F;
    }

    public Vec3 pos() {
        return new Vec3(x, y, z);
    }

    public static float curve(float f, float from, float to) {
        return Mth.clamp((f - from) / (to - from), 0f, 1f);
    }

    public void scaled(float sLifetime, java.util.function.Consumer<EffectContainer> cons) {
        if (time + partialTicks <= sLifetime) {
            float oldLife = this.lifetime;
            this.lifetime = sLifetime;
            cons.accept(this);
            this.lifetime = oldLife;
        }
    }

    public float randRange(long seed, float range) {
        RAND.setSeed(seed * 31L + 17L);
        return (RAND.nextFloat() * 2.0F - 1.0F) * range;
    }

    public Vec3 dir() {
        float yawRad = (float) Math.toRadians(-rotation - 90.0F);
        float pitchRad = (float) Math.toRadians(-pitch);
        float cosPitch = Mth.cos(pitchRad);
        return new Vec3(Mth.cos(yawRad) * cosPitch, Mth.sin(pitchRad), Mth.sin(yawRad) * cosPitch);
    }

    public void randLenVectors(long seed, int amount, double length, BiConsumerDouble cons) {
        RAND.setSeed(seed);
        for (int i = 0; i < amount; i++) {
            double len = RAND.nextDouble() * length;
            double angle = RAND.nextDouble() * Math.PI * 2.0;
            cons.accept(Math.cos(angle) * len, Math.sin(angle) * len);
        }
    }

    public void randLenVectors(long seed, int amount, double length, float angleDeg, float rangeDeg, BiConsumerDouble cons) {
        RAND.setSeed(seed);
        for (int i = 0; i < amount; i++) {
            double len = RAND.nextDouble() * length;
            double a = Math.toRadians(angleDeg + (RAND.nextDouble() * 2.0 - 1.0) * rangeDeg);
            cons.accept(Math.cos(a) * len, Math.sin(a) * len);
        }
    }

    public void randLenVectors3D(long seed, int amount, double length, TriConsumerDouble cons) {
        RAND.setSeed(seed);
        for (int i = 0; i < amount; i++) {
            double len = RAND.nextDouble() * length;
            double u = RAND.nextDouble() * 2.0 - 1.0;
            double theta = RAND.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(Math.max(0.0, 1.0 - u * u));
            cons.accept(r * Math.cos(theta) * len, u * len, r * Math.sin(theta) * len);
        }
    }

    public void randVectorsCone(long seed, int amount, double length, Vec3 direction, double angleDegrees, TriConsumerDouble cons) {
        RAND.setSeed(seed);
        Vec3 norm = direction.lengthSqr() > 1e-4 ? direction.normalize() : new Vec3(0, 1, 0);
        double halfAngleRad = Math.toRadians(angleDegrees / 2.0);
        for (int i = 0; i < amount; i++) {
            double len = RAND.nextDouble() * length;
            double spread = (RAND.nextDouble() * 2.0 - 1.0) * halfAngleRad;
            double roll = RAND.nextDouble() * Math.PI * 2.0;

            Vec3 perp1;
            if (Math.abs(norm.y) < 0.99) {
                perp1 = norm.cross(new Vec3(0, 1, 0)).normalize();
            } else {
                perp1 = norm.cross(new Vec3(1, 0, 0)).normalize();
            }

            Vec3 perp2 = norm.cross(perp1).normalize();
            Vec3 offset = perp1.scale(Math.cos(roll) * Math.sin(spread)).add(perp2.scale(Math.sin(roll) * Math.sin(spread)));
            Vec3 dir = norm.scale(Math.cos(spread)).add(offset).normalize().scale(len);
            cons.accept(dir.x, dir.y, dir.z);
        }
    }

    public ParticleBuilder builder(ParticleType<?> type) {
        return ParticleBuilder.create(type);
    }

    public ParticleBuilder builder(Supplier<? extends ParticleType<?>> type) {
        return ParticleBuilder.create(type);
    }

    public void sound(SoundEvent sound, float volume, float pitch) {
        if (level != null && sound != null) {
            level.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, volume, pitch, false);
        }
    }

    public Vec3 getPosFromData() {
        if (data instanceof Entity entity) {
            return entity.position().add(0, entity.getBbHeight() * 0.5f, 0);
        } else if (data instanceof Vec3 targetPos) {
            return targetPos;
        }

        return Vec3.ZERO;
    }
}
