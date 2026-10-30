package pro.komaru.tridot.common.networking.packets;

import net.minecraft.core.registries.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.network.handling.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.client.networking.*;

public class CooldownSoundPacket implements CustomPacketPayload {
    public static final Type<CooldownSoundPacket> TYPE = Packet.type(Tridot.ID, "cooldown_sound");
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownSoundPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), CooldownSoundPacket::decode);

    private final double posX;
    private final double posY;
    private final double posZ;
    private final SoundEvent event;

    public CooldownSoundPacket(SoundEvent event, double posX, double posY, double posZ) {
        this.event = event;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static CooldownSoundPacket decode(FriendlyByteBuf buf) {
        ResourceLocation soundID = buf.readResourceLocation();
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(soundID);
        return new CooldownSoundPacket(event, buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(CooldownSoundPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandlers.playCooldownSound(msg.getSoundEvent());
            }
        });
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(event.getLocation());
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    public SoundEvent getSoundEvent() {
        return this.event;
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
