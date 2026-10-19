package pro.komaru.tridot.common.networking.packets;

import net.minecraft.client.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.fx.*;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.util.*;

public class PlayEffectPacket implements Packet {
    public static final Type<PlayEffectPacket> TYPE = Packet.type(Tridot.ID, "play_effect");
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayEffectPacket> STREAM_CODEC = Packet.codec(PlayEffectPacket::decode);

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

    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeVarInt(this.effectId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeInt(this.colorPacked);
        buf.writeInt(this.seed);
        buf.writeFloat(this.extraData);
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.targetVec != null);
        if (this.targetVec != null) {
            buf.writeDouble(this.targetVec.x);
            buf.writeDouble(this.targetVec.y);
            buf.writeDouble(this.targetVec.z);
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doOnClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Effect effect = Effect.get(this.effectId);
            Col col = new Col(this.colorPacked);
            Object data = this.extraData;
            if (this.targetVec != null) {
                data = this.targetVec;
            } else if (this.entityId >= 0) {
                Entity ent = mc.level.getEntity(this.entityId);
                if (ent != null) {
                    data = ent;
                }
            }

            EffectHandler.spawn(effect, mc.level, this.x, this.y, this.z, this.yaw, this.pitch, col, data, this.seed);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
