package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.*;
import net.minecraft.network.protocol.common.custom.*;
import net.neoforged.neoforge.network.handling.*;

public abstract class ServerPacket implements CustomPacketPayload{
    public void encode(FriendlyByteBuf buf){
    }

    public final void handle(IPayloadContext context){
        context.enqueueWork(() -> execute(context));
    }

    public void execute(IPayloadContext context){
    }
}
