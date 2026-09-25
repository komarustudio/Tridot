package pro.komaru.tridot.common.networking.packets;

import net.minecraft.client.*;
import net.minecraft.client.sounds.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.neoforge.network.handling.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.client.sound.*;

public class CooldownSoundPacket implements CustomPacketPayload{
    public static final Type<CooldownSoundPacket> TYPE = Packet.type(Tridot.ID, "cooldown_sound");
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownSoundPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), CooldownSoundPacket::decode);

    private final double posX;
    private final double posY;
    private final double posZ;
    private final SoundEvent event;

    public CooldownSoundPacket(SoundEvent event, double posX, double posY, double posZ){
        this.event = event;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static CooldownSoundPacket decode(FriendlyByteBuf buf){
        ResourceLocation soundID = buf.readResourceLocation();
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(soundID);
        return new CooldownSoundPacket(event, buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSound(SoundEvent event){
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if(TridotLibClient.COOLDOWN_SOUND != null && soundManager.isActive(TridotLibClient.COOLDOWN_SOUND)){
            return;
        }

        TridotLibClient.COOLDOWN_SOUND = new TridotSoundInstance(event, Minecraft.getInstance().player);
        soundManager.play(TridotLibClient.COOLDOWN_SOUND);
        if(!soundManager.isActive(TridotLibClient.COOLDOWN_SOUND)){
            TridotLibClient.COOLDOWN_SOUND = null;
        }
    }

    public static void handle(CooldownSoundPacket msg, IPayloadContext ctx){
        ctx.enqueueWork(() -> {
            assert ctx.flow().isClientbound();
            playSound(msg.event);
        });
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeResourceLocation(event.getLocation());
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}