package pro.komaru.tridot.client.render.screenshake;


import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import org.jetbrains.annotations.NotNull;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.common.config.ClientConfig;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.math.ArcRandom;
import pro.komaru.tridot.util.math.Mathf;
import pro.komaru.tridot.util.math.raycast.RayCast;
import pro.komaru.tridot.util.math.raycast.RayCastContext;
import pro.komaru.tridot.util.math.raycast.RayHitResult;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import pro.komaru.tridot.util.phys.Vec3f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScreenshakeHandler{
    public static final List<ScreenshakeInstance> INSTANCES = new CopyOnWriteArrayList<>();
    public static float intensityRotation;
    public static float intensityPosition;
    public static float intensityFov;
    public static float intensityFovNormalize;
    public static Vec3f intensityVector = new Vec3f();
    public static Vec3f intensityVectorOld = new Vec3f();

    public static ArcRandom rand = Tmp.rnd;

    public static void cameraTick(Camera camera){
        if(intensityRotation > 0){
            float yawOffset = randomizeOffset(intensityRotation);
            float pitchOffset = randomizeOffset(intensityRotation);
            camera.setRotation(camera.getYRot() + yawOffset, camera.getXRot() + pitchOffset);
        }

        boolean cameraUpdate = false;
        Tmp.v3.set(camera.getPosition());
        Tmp.v4.set(Tmp.v3);
        if(intensityPosition > 0){
            Tmp.v3.set(intensityPosVec(Tmp.v3));
            cameraUpdate = true;
        }

        if(!intensityVector.isZero() && !intensityVectorOld.isZero()){
            Tmp.v3.set(intensityVec(Tmp.v3));
            cameraUpdate = true;
        }

        if(cameraUpdate){
            Level level = Tridot.PROXY.getLevel();
            if(level != null){
                RayHitResult hitResult = RayCast.getHit(level, new RayCastContext(Tmp.v4, Tmp.v3).setBlockShape(RayCastContext.Block.VISUAL));
                Vec3 hitPos = hitResult.getPos();
                double distance = Math.max(0f, Tmp.v4.dst((float) hitPos.x, (float) hitPos.y, (float) hitPos.z) - 0.1f);

                double dX = Tmp.v4.x() - hitPos.x();
                double dY = Tmp.v4.y() - hitPos.y();
                double dZ = Tmp.v4.z() - hitPos.z();

                float pitch = (float) (Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI);
                float x = (float) (Math.sin(pitch) * Math.cos(Math.atan2(dZ, dX)) * distance);
                float y = (float) (Math.cos(pitch) * distance);
                float z = (float) (Math.sin(pitch) * Math.sin(Math.atan2(dZ, dX)) * distance);
                Tmp.v3.set(Tmp.v4.add(x,y,z));
            }

            camera.setPosition(Tmp.v3.x(), Tmp.v3.y(), Tmp.v3.z());
        }
    }

    private static @NotNull Vec3f intensityVec(Vec3 pos) {
        return Vec3f.from(pos).add(intensityVectorOld.lerp(intensityVector, ClientTick.partialTicks));
    }

    private static @NotNull Vec3f intensityVec(Vec3f pos) {
        float pt = ClientTick.partialTicks;
        float lx = Mth.lerp(pt, intensityVectorOld.x, intensityVector.x);
        float ly = Mth.lerp(pt, intensityVectorOld.y, intensityVector.y);
        float lz = Mth.lerp(pt, intensityVectorOld.z, intensityVector.z);
        return pos.add(lx, ly, lz);
    }

    private static @NotNull Vec3f intensityPosVec(Vec3 pos) {
        return Vec3f.from(pos).add(intensityVectorOld.lerp(intensityVector, ClientTick.partialTicks));
    }

    private static @NotNull Vec3f intensityPosVec(Vec3f pos) {
        float angleA = rand.nextFloat() * Mathf.pi * 2f;
        float angleB = rand.nextFloat() * Mathf.pi * 2f;
        float x = (float)(Math.cos(angleA) * Math.cos(angleB)) * randomizeOffset(intensityPosition);
        float y = (float)(Math.sin(angleA) * Math.cos(angleB)) * randomizeOffset(intensityPosition);
        float z = (float)Math.sin(angleB) * randomizeOffset(intensityPosition);
        return pos.add(x,y,z);
    }

    public static void fovTick(ComputeFovModifierEvent event){
        float fovModifier = event.getFovModifier();
        if(fovModifier != event.getNewFovModifier()) fovModifier = event.getNewFovModifier();
        boolean update = false;
        if(intensityFov >= 0){
            float offset = randomizeOffset(intensityFov);
            fovModifier = fovModifier + offset;
            update = true;
        }

        if(intensityFovNormalize != 0){
            fovModifier = fovModifier + intensityFovNormalize;
            update = true;
        }

        if(update) event.setNewFovModifier((float)Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, fovModifier));
    }

    public static float apply(ScreenshakeInstance instance, float current, float update) {
        if (instance.isNormalize) {
            return current + update;
        } else {
            return update;
        }
    }

    public static void clientTick(Camera camera){
        float intensity = (float) (double) ClientConfig.SCREENSHAKE_INTENSITY.get();
        float rotationNormalize = 0;
        float positionNormalize = 0;
        float fovNormalize = 0;
        float fovNorm = 0;
        float rotation = 0;
        float position = 0;
        float fov = 0;

        Tmp.v3.clear();
        for(ScreenshakeInstance instance : INSTANCES){
            float update = instance.updateIntensity(camera);
            if(instance.isRotation) rotation = apply(instance, rotationNormalize, update);
            if(instance.isPosition) position = apply(instance, positionNormalize, update);
            if(instance.isFov){
                if(instance.isNormalize){
                    fovNormalize = fovNormalize + update;
                }else{
                    if(instance.isFovNormalize){
                        fovNorm = fovNorm + update;
                    }else{
                        fov = fov + update;
                    }
                }
            }

            if(instance.isVector){
                Tmp.v4.set(instance.vx, instance.vy, instance.vz).scale(update * intensity);
                if (instance.isNormalize) {
                    Tmp.v3.add(Tmp.v4.sub(Tmp.v3));
                } else {
                    Tmp.v3.add(Tmp.v4);
                }
            }
        }

        rotationNormalize = Math.min(rotationNormalize, intensity);
        positionNormalize = Math.min(positionNormalize, intensity);
        fovNorm = Math.min(fovNorm, intensity);
        rotation = rotation * intensity;
        position = position * intensity;
        fov = fov * intensity;

        intensityRotation = (float)Math.max(Math.pow(rotationNormalize, 3f), rotation);
        intensityPosition = (float)Math.max(Math.pow(positionNormalize / 2f, 3f), position);
        intensityFov = Math.max(fovNorm, fov);
        intensityFovNormalize = fovNormalize;
        intensityVectorOld.set(intensityVector);
        intensityVector.set(Tmp.v3);
        INSTANCES.removeIf(i -> i.progress >= i.duration);
    }

    public static void add(ScreenshakeInstance instance){
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
        INSTANCES.add(instance);
    }

    public static void add(Level level, ScreenshakeInstance instance){
        if (level != null && !level.isClientSide) {
            if (instance instanceof PositionedScreenshakeInstance pos) {
                PacketHandler.sendToTracking(level, BlockPos.containing(pos.position.x, pos.position.y, pos.position.z), new ScreenshakePacket(pos));
            } else {
                PacketHandler.sendToAll(new ScreenshakePacket(instance));
            }

            return;
        }

        add(instance);
    }

    public static void add(Player player, ScreenshakeInstance instance){
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendTo(serverPlayer, new ScreenshakePacket(instance));
            return;
        }

        add(instance);
    }

    public static float randomizeOffset(float offset){
        if(offset == 0) return 0f;
        return rand.nextFloat(-offset * 2, offset * 2);
    }
}
