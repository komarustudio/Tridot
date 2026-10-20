package pro.komaru.tridot.common.networking.packets;

import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;


import net.minecraftforge.network.NetworkEvent;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.fx.Effect;
import pro.komaru.tridot.client.gfx.EffectHandler;
import pro.komaru.tridot.util.Col;

import java.util.function.Supplier;

public class PlayEffectPacket {
    private final int effectId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final int colorPacked;
    private final int seed;
    private final float extraData;
    private final int entityId;
    private final Vec3 targetVec;

    public PlayEffectPacket(int effectId, double x, double y, double z, float yaw, float pitch, int colorPacked, int seed, float extraData, int entityId, Vec3 targetVec) {
        this.effectId = effectId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.colorPacked = colorPacked;
        this.seed = seed;
        this.extraData = extraData;
        this.entityId = entityId;
        this.targetVec = targetVec;
    }

    public static void encode(PlayEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.effectId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeInt(msg.colorPacked);
        buf.writeInt(msg.seed);
        buf.writeFloat(msg.extraData);
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.targetVec != null);
        if (msg.targetVec != null) {
            buf.writeDouble(msg.targetVec.x);
            buf.writeDouble(msg.targetVec.y);
            buf.writeDouble(msg.targetVec.z);
        }
    }

    public static PlayEffectPacket decode(FriendlyByteBuf buf) {
        int effectId = buf.readVarInt();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        int colorPacked = buf.readInt();
        int seed = buf.readInt();
        float extraData = buf.readFloat();
        int entityId = buf.readVarInt();
        Vec3 targetVec = buf.readBoolean() ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()) : null;
        return new PlayEffectPacket(effectId, x, y, z, yaw, pitch, colorPacked, seed, extraData, entityId, targetVec);
    }

    public static void handle(PlayEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                var level = Tridot.PROXY.getLevel();
                if (level != null) {
                    Effect effect = Effect.get(msg.effectId);
                    Col col = new Col(msg.colorPacked);
                    Object data = msg.extraData;
                    if (msg.targetVec != null) {
                        data = msg.targetVec;
                    } else if (msg.entityId >= 0) {
                        Entity ent = level.getEntity(msg.entityId);
                        if (ent != null) {
                            data = ent;
                        }
                    }

                    EffectHandler.spawn(effect, level, msg.x, msg.y, msg.z, msg.yaw, msg.pitch, col, data, msg.seed);
                }
            });
        }

        ctx.get().setPacketHandled(true);
    }
}
