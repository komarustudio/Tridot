package pro.komaru.tridot.util.struct.stash.net;

import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.util.struct.stash.*;
import net.minecraft.network.*;
import pro.komaru.tridot.api.networking.Packet;

public class SyncStashObjectPacket implements Packet {
    public static final Type<SyncStashObjectPacket> TYPE = Packet.type(Tridot.ID, "sync_stash_object");
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStashObjectPacket> STREAM_CODEC = Packet.codec(SyncStashObjectPacket::new);

    int id;
    byte[] bytes;
    public SyncStashObjectPacket(int id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }
    public SyncStashObjectPacket(FriendlyByteBuf buf) {
        this.id = buf.readInt();
        this.bytes = buf.readByteArray();
    }
    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeByteArray(bytes);
    }

    @Override
    public void doOnClient() {
        SyncStash.set(id,bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
