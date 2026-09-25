package pro.komaru.tridot.client.render.gui.particle;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.*;
import org.joml.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.options.*;
import pro.komaru.tridot.client.gfx.particle.type.*;
import pro.komaru.tridot.client.render.gui.particle.ParticleEmitterHandler.*;
import pro.komaru.tridot.common.config.*;

import java.util.*;

public class ScreenParticleHandler{
    public static final Map<ScreenParticleItemStackKey, ScreenParticleHolder> ITEM_PARTICLES = new HashMap<>();
    public static final Map<ScreenParticleItemStackRetrievalKey, ItemStack> ITEM_STACK_CACHE = new HashMap<>();
    public static final Collection<ScreenParticleItemStackRetrievalKey> ACTIVELY_ACCESSED_KEYS = new ArrayList<>();

    public static ScreenParticleHolder cachedItemParticles = null;
    public static int currentItemX, currentItemY;

    public static final Tesselator TESSELATOR = new Tesselator();
    public static boolean canSpawnParticles;
    public static boolean renderingHotbar;

    public static void tickParticles() {
        if (!ClientConfig.ITEM_GUI_PARTICLE.get()) return;
        ITEM_PARTICLES.values().forEach(ScreenParticleHolder::tick);
        ITEM_PARTICLES.values().removeIf(ScreenParticleHolder::isEmpty);

        ITEM_STACK_CACHE.keySet().removeIf(k -> !ACTIVELY_ACCESSED_KEYS.contains(k));
        ACTIVELY_ACCESSED_KEYS.clear();
        canSpawnParticles = true;
    }

    public static void renderTick(RenderFrameEvent.Post event) {
        canSpawnParticles = false;
    }

    public static void renderItemStackEarly(PoseStack poseStack, ItemStack stack, int x, int y) {
        if (!ClientConfig.ITEM_GUI_PARTICLE.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            if (minecraft.isPaused()) {
                return;
            }

            if (!stack.isEmpty()) {
                List<IGUIParticleItem> emitters = ParticleEmitterHandler.EMITTERS.get(stack.getItem());
                if (emitters != null) {
                    final Matrix4f pose = poseStack.last().pose();
                    int xOffset = (int) (8 + pose.m30());
                    int yOffset = (int) (8 + pose.m31());
                    currentItemX = x + xOffset;
                    currentItemY = y + yOffset;
                    for (IGUIParticleItem emitter : emitters) {
                        ScreenParticleHandler.renderParticles(ScreenParticleHandler.spawnAndPullParticles(minecraft.level, emitter, stack, false));
                        cachedItemParticles = ScreenParticleHandler.spawnAndPullParticles(minecraft.level, emitter, stack, true);
                    }
                }
            }
        }
    }

    public static ScreenParticleHolder spawnAndPullParticles(ClientLevel level, IGUIParticleItem emitter, ItemStack stack, boolean isRenderedAfterItem) {
        ScreenParticleItemStackRetrievalKey cacheKey = new ScreenParticleItemStackRetrievalKey(renderingHotbar, isRenderedAfterItem, currentItemX, currentItemY);
        ScreenParticleHolder target = ITEM_PARTICLES.computeIfAbsent(new ScreenParticleItemStackKey(renderingHotbar, isRenderedAfterItem, stack), s -> new ScreenParticleHolder());
        pullFromParticleVault(cacheKey, stack, target, isRenderedAfterItem);
        if (canSpawnParticles) {
            if (isRenderedAfterItem) {
                emitter.spawnParticlesLate(target, level, ClientTick.mcPartialTick(), stack, currentItemX, currentItemY);
            } else {
                emitter.spawnParticlesEarly(target, level, ClientTick.mcPartialTick(), stack, currentItemX, currentItemY);
            }
        }

        ACTIVELY_ACCESSED_KEYS.add(cacheKey);
        return target;
    }

    public static void pullFromParticleVault(ScreenParticleItemStackRetrievalKey cacheKey, ItemStack currentStack, ScreenParticleHolder target, boolean isRenderedAfterItem) {
        if (ITEM_STACK_CACHE.containsKey(cacheKey)) {
            ItemStack oldStack = ITEM_STACK_CACHE.get(cacheKey);
            if (oldStack != currentStack && oldStack.getItem().equals(currentStack.getItem())) {
                ScreenParticleItemStackKey oldKey = new ScreenParticleItemStackKey(renderingHotbar, isRenderedAfterItem, oldStack);
                ScreenParticleHolder oldParticles = ITEM_PARTICLES.get(oldKey);
                if (oldParticles != null) {
                    target.addFrom(oldParticles);
                }

                ITEM_STACK_CACHE.remove(cacheKey);
                ITEM_PARTICLES.remove(oldKey);
            }
        }

        ITEM_STACK_CACHE.put(cacheKey, currentStack);
    }

    public static void renderItemStackLate() {
        if (cachedItemParticles != null) {
            renderParticles(cachedItemParticles);
            cachedItemParticles = null;
        }
    }

    // PORT NOTE: Tesselator.getBuilder()/end() are gone; each render type begins a fresh BufferBuilder and uploads it.
    public static void renderParticles(ScreenParticleHolder screenParticleTarget) {
        screenParticleTarget.particles.forEach((renderType, particles) -> {
            BufferBuilder builder = renderType.begin(TESSELATOR, Minecraft.getInstance().getTextureManager());
            for (ScreenParticle next : particles) {
                next.render(builder);
            }

            renderType.end(builder);
        });
    }

    public static void clearParticles() {
        ITEM_PARTICLES.values().forEach(ScreenParticleHandler::clearParticles);
    }

    public static void clearParticles(ScreenParticleHolder screenParticleTarget) {
        screenParticleTarget.particles.values().forEach(ArrayList::clear);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ScreenParticleOptions> ScreenParticle addParticle(ScreenParticleHolder screenParticleTarget, T options, double x, double y, double xMotion, double yMotion) {
        Minecraft minecraft = Minecraft.getInstance();
        ScreenParticleType<T> type = (ScreenParticleType<T>) options.type;
        ScreenParticle particle = type.provider.createParticle(minecraft.level, options, x, y, xMotion, yMotion);
        ArrayList<ScreenParticle> list = screenParticleTarget.particles.computeIfAbsent(options.renderType, (a) -> new ArrayList<>());
        list.add(particle);
        return particle;
    }
}
