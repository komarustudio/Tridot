package pro.komaru.tridot.api.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.client.gfx.EffectHandler;
import pro.komaru.tridot.common.networking.packets.PlayEffectPacket;
import pro.komaru.tridot.util.Col;

import java.util.ArrayList;
import java.util.List;

/**
 * Functional visual effect definition directly inspired by Mindustry's Effect system.
 */
public class Effect {
    private static final List<Effect> ALL = new ArrayList<>();
    public final int id;
    public float lifetime;
    public float clip = 64.0F;

    public final EffectRenderer renderer;
    public EffectDrawer drawer;

    @FunctionalInterface
    public interface EffectDrawer {
        void draw(EffectContainer e, PoseStack pose);
    }

    @FunctionalInterface
    public interface EffectRenderer {
        void render(EffectContainer e);
    }

    public Effect(float lifetime, EffectRenderer renderer) {
        this(lifetime, 64.0F, renderer);
    }

    public Effect draw(EffectDrawer drawer) {
        this.drawer = drawer;
        return this;
    }

    public Effect(float lifetime, EffectDrawer drawer) {
        this(lifetime, 64.0F, drawer);
    }

    public Effect(float lifetime, float clip, EffectDrawer drawer) {
        this(lifetime, clip, e -> {});
        this.drawer = drawer;
    }

    public Effect(float lifetime, float clip, EffectRenderer renderer) {
        this.lifetime = lifetime;
        this.clip = clip;
        this.renderer = renderer;
        synchronized (ALL) {
            this.id = ALL.size();
            ALL.add(this);
        }
    }

    public static Effect get(int id) {
        synchronized (ALL) {
            if (id >= 0 && id < ALL.size()) {
                return ALL.get(id);
            }
        }
        return Fx.none;
    }

    public void at(Entity entity) {
        at(entity, Col.white);
    }

    public void at(Entity entity, Col color) {
        if (entity == null) return;
        at(entity.level(), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                entity.getYRot(), entity.getXRot(), color, entity);
    }

    public void at(Level level, double x, double y, double z) {
        at(level, x, y, z, 0.0F, 0.0F, Col.white, null);
    }

    public void at(Level level, Vec3 pos) {
        at(level, pos.x, pos.y, pos.z, 0.0F, 0.0F, Col.white, null);
    }

    public void at(Level level, Vec3 pos, Object data) {
        at(level, pos.x, pos.y, pos.z, 0.0F, 0.0F, Col.white, data);
    }

    public void at(Level level, Vec3 pos, Col color) {
        at(level, pos.x, pos.y, pos.z, 0.0F, 0.0F, color, null);
    }

    public void at(Level level, Vec3 pos, Col color, Object data) {
        at(level, pos.x, pos.y, pos.z, 0.0F, 0.0F, color, data);
    }

    public void at(Level level, Vec3 pos, Vec3 dir) {
        at(level, pos, dir, Col.white, null);
    }

    public void at(Level level, Vec3 pos, Vec3 dir, Col color) {
        at(level, pos, dir, color, null);
    }

    public void at(Level level, Vec3 pos, Vec3 dir, Col color, Object data) {
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180.0 / Math.PI)));
        at(level, pos.x, pos.y, pos.z, yaw, pitch, color, data);
    }

    public void at(Level level, double x, double y, double z, float yaw, float pitch, Col color, Object data) {
        if (level == null) return;
        int seed = (int) (Math.random() * 100000);
        Col c = color != null ? color : Col.white;

        if (!level.isClientSide) {
            float extra = data instanceof Number num ? num.floatValue() : 0.0F;
            int entityId = data instanceof Entity ent ? ent.getId() : -1;
            Vec3 targetVec = data instanceof Vec3 vec ? vec : null;
            PacketHandler.sendToTracking(level, BlockPos.containing(x, y, z),
                    new PlayEffectPacket(this.id, x, y, z, yaw, pitch, c.pack(), seed, extra, entityId, targetVec));
            return;
        }

        EffectHandler.spawn(this, level, x, y, z, yaw, pitch, c, data, seed);
    }
}
