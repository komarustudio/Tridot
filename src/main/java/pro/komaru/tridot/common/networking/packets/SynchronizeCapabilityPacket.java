package pro.komaru.tridot.common.networking.packets;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.api.capabilities.CapabilityEntry;
import pro.komaru.tridot.api.capabilities.Capabilities;
import pro.komaru.tridot.api.networking.Packet;
import pro.komaru.tridot.util.struct.Structs;
import pro.komaru.tridot.util.struct.capability.CapImpl;

public class SynchronizeCapabilityPacket implements Packet {
    public static final Type<SynchronizeCapabilityPacket> TYPE = Packet.type(Tridot.ID, "synchronize_capability");
    public static final StreamCodec<RegistryFriendlyByteBuf, SynchronizeCapabilityPacket> STREAM_CODEC = Packet.codec(SynchronizeCapabilityPacket::new);

    CompoundTag nbt;
    int id;
    public SynchronizeCapabilityPacket(CapImpl impl, int id, HolderLookup.Provider provider) {
        this(impl.serializeNBT(provider),id);
    }
    public SynchronizeCapabilityPacket(CompoundTag tag,int id) {
        nbt = tag;
        this.id = id;
    }
    public SynchronizeCapabilityPacket(FriendlyByteBuf buf) {
        id = buf.readInt();
        nbt = buf.readNbt();
    }
    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeNbt(nbt);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doOnClient() {
        Structs.safeRun(Utils.player(), p -> {
            CapabilityEntry<CapImpl> entry = (CapabilityEntry<CapImpl>) Capabilities.caps.get(id);
            p.getData(entry.type()).deserializeNBT(p.registryAccess(), nbt);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
