package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.client.cinema.CutsceneHelper;

public class CutsceneSkippedPacket implements CustomPacketPayload{
    public static final Type<CutsceneSkippedPacket> TYPE = Packet.type(Tridot.ID, "cutscene_skipped");
    public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneSkippedPacket> STREAM_CODEC = StreamCodec.of(CutsceneSkippedPacket::encode, CutsceneSkippedPacket::decode);

    public CutsceneSkippedPacket(){}

    public static void encode(FriendlyByteBuf buffer, CutsceneSkippedPacket object){}

    public static CutsceneSkippedPacket decode(FriendlyByteBuf buffer){
        return new CutsceneSkippedPacket();
    }

    public static void handle(CutsceneSkippedPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(!(ctx.player() instanceof ServerPlayer player)) return;
            CutsceneHelper.stop(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}