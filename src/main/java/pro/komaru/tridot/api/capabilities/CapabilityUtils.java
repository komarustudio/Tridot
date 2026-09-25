package pro.komaru.tridot.api.capabilities;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import pro.komaru.tridot.common.networking.packets.SynchronizeCapabilityPacket;
import pro.komaru.tridot.util.struct.capability.CapImpl;
import pro.komaru.tridot.util.struct.data.Var;
import pro.komaru.tridot.util.struct.func.Cons;

public class CapabilityUtils {
    @SuppressWarnings("unchecked")
    public static SynchronizeCapabilityPacket syncPacket(ServerPlayer player, CapabilityEntry<?> entry) {
        Var<SynchronizeCapabilityPacket> var = new Var<>(null);
        getNoSync(player,(CapabilityEntry<CapImpl>) entry, i -> var.var = new SynchronizeCapabilityPacket(i,entry.id, player.registryAccess()));
        return var.var;
    }

    public static <T extends CapImpl> void getNoSync(Player player, CapabilityEntry<T> entry, Cons<T> impl) {
        impl.get(player.getData(entry.type()));
    }
    public static <T extends CapImpl> void get(Player player, CapabilityEntry<T> entry, Cons<T> impl) {
        T e = player.getData(entry.type());
        impl.get(e);
        if(player instanceof ServerPlayer p) e.sync(p);
    }
    public static <T extends CapImpl> void get(Player player, Class<T> clazz, CapabilityEntry<T> entry, Cons<T> impl) {
        get(player, entry, impl);
    }
}
