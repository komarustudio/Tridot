package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.handling.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.render.*;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.Log;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.math.ArcRandom;

import java.awt.*;
import java.util.*;

public class DashParticlePacket implements CustomPacketPayload{
    public static final Type<DashParticlePacket> TYPE = Packet.type(Tridot.ID, "dash_particle");
    public static final StreamCodec<RegistryFriendlyByteBuf, DashParticlePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), DashParticlePacket::decode);

    private final UUID id;
    private final float velX, velY, velZ;
    private final int count, colorR, colorG, colorB;

    public DashParticlePacket(UUID id, int count, float velX, float velY, float velZ, int colorR, int colorG, int colorB) {
        this.id = id;
        this.count = count;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
    }

    public DashParticlePacket(UUID id, int count, float velX, float velY, float velZ, Color clr) {
        this.id = id;
        this.count = count;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.colorR = clr.getRed();
        this.colorG = clr.getGreen();
        this.colorB = clr.getBlue();
    }

    public static DashParticlePacket decode(FriendlyByteBuf buf) {
        return new DashParticlePacket(buf.readUUID(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(DashParticlePacket msg, IPayloadContext ctx) {
        if (ctx.flow().isClientbound()) {
            ctx.enqueueWork(() -> {
                Level level = Tridot.PROXY.getLevel();
                Player player = level.getPlayerByUUID(msg.id);
                if (player != null) {
                    ArcRandom rand = Tmp.rnd;
                    Col color = new Col(msg.colorR, msg.colorG, msg.colorB);
                    Vec3 pos = new Vec3(player.getX(), player.getY(), player.getZ());
                    for (int count = 0; count < msg.count; count++) {
                        double pitch = ((player.getRotationVector().x + 90) * Math.PI) / 180;
                        double yaw = ((player.getRotationVector().y + 90) * Math.PI) / 180;
                        for (int i = 0; i < 10; i += 1) {
                            double locDistance = i * 0.5D;
                            double X = Math.sin(pitch) * Math.cos(yaw) * locDistance;
                            double Y = Math.cos(pitch) * 2;
                            double Z = Math.sin(pitch) * Math.sin(yaw) * locDistance;

                            ParticleBuilder.create(TridotParticles.WISP)
                            .setColorData(ColorParticleData.create(color, Col.fromHex("a2a2a2")).build())
                            .setRenderType(TridotRenderTypes.ADDITIVE_PARTICLE)
                            .setTransparencyData(GenericParticleData.create(rand.nextFloat(0, 0.6f), 0f).build())
                            .setScaleData(GenericParticleData.create(0.92f, 0f).build())
                            .setLifetime(15)
                            .randomVelocity(msg.velX, msg.velY, msg.velZ)
                            .randomOffset(0.025f, 0, 0.025f)
                            .spawn(level, pos.x + X + (rand.nextDouble() - 0.5D), pos.y + Y, pos.z + Z + (rand.nextDouble() - 0.5D));
                        }
                    }
                } else {
                    Log.error("Player with UUID {}, not found", msg.id);
                }
            });
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeInt(count);
        buf.writeFloat(velX);
        buf.writeFloat(velY);
        buf.writeFloat(velZ);

        buf.writeInt(colorR);
        buf.writeInt(colorG);
        buf.writeInt(colorB);
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}