package pro.komaru.tridot.client.gfx;

import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.resources.*;
import net.neoforged.neoforge.client.event.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.client.gfx.particle.options.*;
import pro.komaru.tridot.client.gfx.particle.screen.*;
import pro.komaru.tridot.client.gfx.particle.type.*;

import java.util.*;

public class TridotScreenParticles{
    public static final ArrayList<ScreenParticleType<?>> PARTICLE_TYPES = new ArrayList<>();

    public static ScreenParticleType<ScreenParticleOptions> WISP = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> TINY_WISP = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> SPARKLE = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> STAR = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> SQUARE = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> DOT = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> CIRCLE = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> TINY_CIRCLE = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> HEART = registerType(new TridotScreenParticleType());
    public static ScreenParticleType<ScreenParticleOptions> SKULL = registerType(new TridotScreenParticleType());

    public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
        registerProvider(WISP, new TridotScreenParticleType.Factory(Tridot.ofTridot("wisp")));
        registerProvider(TINY_WISP, new TridotScreenParticleType.Factory(Tridot.ofTridot("tiny_wisp")));
        registerProvider(SPARKLE, new TridotScreenParticleType.Factory(Tridot.ofTridot("sparkle")));
        registerProvider(STAR, new TridotScreenParticleType.Factory(Tridot.ofTridot("star")));
        registerProvider(SQUARE, new TridotScreenParticleType.Factory(Tridot.ofTridot("square")));
        registerProvider(DOT, new TridotScreenParticleType.Factory(Tridot.ofTridot("dot")));
        registerProvider(CIRCLE, new TridotScreenParticleType.Factory(Tridot.ofTridot("circle")));
        registerProvider(TINY_CIRCLE, new TridotScreenParticleType.Factory(Tridot.ofTridot("tiny_circle")));
        registerProvider(HEART, new TridotScreenParticleType.Factory(Tridot.ofTridot("heart")));
        registerProvider(SKULL, new TridotScreenParticleType.Factory(Tridot.ofTridot("skull")));
    }

    public static <T extends ScreenParticleOptions> ScreenParticleType<T> registerType(ScreenParticleType<T> type) {
        PARTICLE_TYPES.add(type);
        return type;
    }

    public static <T extends ScreenParticleOptions> void registerProvider(ScreenParticleType<T> type, ScreenParticleType.ParticleProvider<T> provider) {
        type.provider = provider;
    }

    public static SpriteSet getSpriteSet(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().particleEngine.spriteSets.get(resourceLocation);
    }
}
