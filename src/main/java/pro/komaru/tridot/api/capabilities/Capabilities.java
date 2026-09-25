package pro.komaru.tridot.api.capabilities;

import com.mojang.logging.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.util.struct.capability.CapImpl;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.data.Var;
import pro.komaru.tridot.util.struct.func.Prov;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class Capabilities {

    public static Seq<CapabilityEntry<?>> caps = Seq.with();

    private static final Var<String> tempMod = new Var<>("");
    public static void begin(String modId) {
        tempMod.var = modId;
    }
    public static <T extends CapImpl> CapabilityEntry<T> reg(String modId, String id, Prov<T> factory) {
        CapabilityEntry<T> entry = new CapabilityEntry<>();
        entry.capId = id;
        entry.modId = modId;
        entry.factory = factory;
        entry.id = caps.size;
        if(caps.contains(entry))
            LogUtils.getLogger().warn("Existing capability register: {}:{}", tempMod, id);
        caps.addUnique(entry);
        return entry;
    }
    public static void close(String modId) {
        if(modId.equals(tempMod.var)) tempMod.var = null;
    }

    /** Called from the Tridot entrypoint with the mod bus. */
    public static void register(IEventBus modBus) {
        modBus.addListener(Capabilities::onRegister);
    }

    private static void onRegister(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, helper -> {
            for (CapabilityEntry<?> cap : caps) {
                helper.register(cap.location(), cap.buildType());
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.getEntity());
    }

    @SuppressWarnings("unchecked")
    public static void sync(Player player) {
        if(player instanceof ServerPlayer s)
            for (CapabilityEntry<?> cap : caps)
                CapabilityUtils.get(player,(CapabilityEntry<CapImpl>) cap, i -> i.sync(s));
    }
}
