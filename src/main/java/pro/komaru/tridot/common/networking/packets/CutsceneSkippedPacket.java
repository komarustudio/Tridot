package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import pro.komaru.tridot.client.cinema.CutsceneHelper;

import java.util.function.Supplier;

public class CutsceneSkippedPacket{
    public CutsceneSkippedPacket(){}

    public static void encode(CutsceneSkippedPacket object, FriendlyByteBuf buffer){}

    public static CutsceneSkippedPacket decode(FriendlyByteBuf buffer){
        return new CutsceneSkippedPacket();
    }

    public void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if(player == null) return;
            CutsceneHelper.stop(player);
        });

        ctx.get().setPacketHandled(true);
    }
}