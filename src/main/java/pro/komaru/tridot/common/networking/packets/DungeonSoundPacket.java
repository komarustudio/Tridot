package pro.komaru.tridot.common.networking.packets;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.client.networking.ClientPacketHandlers;

public class DungeonSoundPacket implements CustomPacketPayload {
    public static final Type<DungeonSoundPacket> TYPE = Packet.type(Tridot.ID, "dungeon_sound");
    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonSoundPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), DungeonSoundPacket::decode);

    private final double posX;
    private final double posY;
    private final double posZ;
    private final SoundEvent event;

    public DungeonSoundPacket(SoundEvent event, double posX, double posY, double posZ) {
        this.event = event;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static DungeonSoundPacket decode(FriendlyByteBuf buf) {
        ResourceLocation soundID = buf.readResourceLocation();
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(soundID);
        return new DungeonSoundPacket(event, buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(DungeonSoundPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandlers.handleDungeonSound(msg);
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
