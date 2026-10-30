package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.client.networking.ClientPacketHandlers;

public class ParryParticlePacket implements CustomPacketPayload {
    public static final Type<ParryParticlePacket> TYPE = Packet.type(Tridot.ID, "parry_particle");
    public static final StreamCodec<RegistryFriendlyByteBuf, ParryParticlePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), ParryParticlePacket::decode);

    private final double posX, posY, posZ;

    public ParryParticlePacket(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static ParryParticlePacket decode(FriendlyByteBuf buf) {
        return new ParryParticlePacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ParryParticlePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandlers.handleParryParticle(msg);
            }
        });
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
