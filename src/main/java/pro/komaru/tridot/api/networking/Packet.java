package pro.komaru.tridot.api.networking;

import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.network.handling.*;

import java.util.function.*;

public interface Packet extends CustomPacketPayload {
    default void save(FriendlyByteBuf buf) {

    }

    default void handle(IPayloadContext ctx, ServerPlayer sender) {

    }

    default void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(FMLEnvironment.dist.isClient()) doOnClient();
            handle(ctx, ctx.player() instanceof ServerPlayer sp ? sp : null);
        });
    }

    @OnlyIn(Dist.CLIENT)
    default void doOnClient() {

    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String modId, String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(modId, path));
    }

    /** Builds a stream codec from {@link #save} and a buffer-reading constructor. */
    static <T extends Packet> StreamCodec<RegistryFriendlyByteBuf, T> codec(Function<FriendlyByteBuf, T> reader) {
        return StreamCodec.of((buf, packet) -> packet.save(buf), reader::apply);
    }
}
