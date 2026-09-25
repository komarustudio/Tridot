package pro.komaru.tridot.client.gfx;

import net.minecraft.core.registries.*;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.client.gfx.particle.type.*;
import net.minecraft.core.particles.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.client.gfx.particle.GenericParticle;
import pro.komaru.tridot.client.gfx.particle.ICustomParticleRender;
import pro.komaru.tridot.client.gfx.particle.behavior.ICustomBehaviorParticleRender;
import pro.komaru.tridot.client.render.LevelRenderHandler;

public class TridotParticles{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Tridot.ID);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> WISP = PARTICLES.register("wisp", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> TINY_WISP = PARTICLES.register("tiny_wisp", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> SPARKLE = PARTICLES.register("sparkle", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> STAR = PARTICLES.register("star", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> TINY_STAR = PARTICLES.register("tiny_star", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> SQUARE = PARTICLES.register("square", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> DOT = PARTICLES.register("dot", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> CIRCLE = PARTICLES.register("circle", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> TINY_CIRCLE = PARTICLES.register("tiny_circle", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> HEART = PARTICLES.register("heart", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> SKULL = PARTICLES.register("skull", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> SMOKE = PARTICLES.register("smoke", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, GenericParticleType> TRAIL = PARTICLES.register("trail", GenericParticleType::new);
    public static DeferredHolder<ParticleType<?>, ItemParticleType> ITEM = PARTICLES.register("item", ItemParticleType::new);
    public static DeferredHolder<ParticleType<?>, BlockParticleType> BLOCK = PARTICLES.register("block", BlockParticleType::new);
    public static DeferredHolder<ParticleType<?>, FluidParticleType> FLUID = PARTICLES.register("fluid", FluidParticleType::new);
    public static DeferredHolder<ParticleType<?>, SpriteParticleType> SPRITE = PARTICLES.register("sprite", SpriteParticleType::new);
    public static DeferredHolder<ParticleType<?>, LeavesParticleType> CHERRY_LEAVES = PARTICLES.register("cherry_leaves", LeavesParticleType::new);

    public static void register(IEventBus eventBus){
        PARTICLES.register(eventBus);
    }

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistryEvents{
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event){
            event.registerSpriteSet(WISP.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(TINY_WISP.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(SPARKLE.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(STAR.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(TINY_STAR.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(SQUARE.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(DOT.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(CIRCLE.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(TINY_CIRCLE.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(HEART.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(SKULL.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(SMOKE.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(TRAIL.get(), GenericParticleType.Factory::new);
            event.registerSpriteSet(ITEM.get(), ItemParticleType.Factory::new);
            event.registerSpriteSet(BLOCK.get(), BlockParticleType.Factory::new);
            event.registerSpriteSet(FLUID.get(), FluidParticleType.Factory::new);
            event.registerSpriteSet(SPRITE.get(), SpriteParticleType.Factory::new);
            event.registerSpriteSet(CHERRY_LEAVES.get(), LeavesParticleType.Factory::new);
        }
    }

    public static void addParticleList(ICustomParticleRender particle){
        LevelRenderHandler.particleList.add(particle);
    }

    public static void addBehaviorParticleList(GenericParticle particle, ICustomBehaviorParticleRender behavior){
        LevelRenderHandler.behaviorParticleList.put(particle, behavior);
    }
}
