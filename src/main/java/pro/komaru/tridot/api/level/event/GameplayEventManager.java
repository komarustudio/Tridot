package pro.komaru.tridot.api.level.event;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.storage.*;
import pro.komaru.tridot.util.struct.data.*;

public class GameplayEventManager extends SavedData{
    public static final Seq<ResourceLocation> activeEvents = Seq.with();
    public static final SavedData.Factory<GameplayEventManager> FACTORY = new SavedData.Factory<>(GameplayEventManager::new, GameplayEventManager::load, null);
    private transient int tickCounter = 0;

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ResourceLocation id : activeEvents) {
            list.add(StringTag.valueOf(id.toString()));
        }

        nbt.put("ActiveEvents", list);
        return nbt;
    }

    public static GameplayEventManager load(CompoundTag nbt, HolderLookup.Provider registries) {
        GameplayEventManager manager = new GameplayEventManager();
        ListTag list = nbt.getList("ActiveEvents", 8);
        for (int i = 0; i < list.size(); i++) {
            activeEvents.add(ResourceLocation.parse(list.getString(i)));
        }

        return manager;
    }

    public static GameplayEventManager get(ServerLevel server) {
        DimensionDataStorage storage = server.getDataStorage();
        return storage.computeIfAbsent(FACTORY, "gameplay_events");
    }

    public boolean isEventActive(ResourceLocation eventId) {
        return activeEvents.contains(eventId);
    }

    public void tickAllEvents(ServerLevel server) {
        if (tickCounter++ % 20 != 0) {
            return;
        }

        for (ResourceLocation eventId : activeEvents) {
            GameplayEvent event = GameplayEvents.get(eventId);
            if (event == null) continue;

            boolean conditionsMet = event.test(server);
            boolean wasActive = activeEvents.contains(eventId);
            if (conditionsMet && !wasActive) {
                startEvent(eventId, server);
            } else if (!conditionsMet && wasActive) {
                stopEvent(eventId, server);
            }
        }
    }

    public void tickActiveEvents(ServerLevel server) {
        activeEvents.forEach((id) -> {
            GameplayEvent event = GameplayEvents.get(id);
            if (event != null) {
                event.onTick(server);
            }
        });
    }

    public boolean startEvent(ResourceLocation eventId, ServerLevel server) {
        if (activeEvents.contains(eventId)) {
            return false;
        }

        GameplayEvent event = GameplayEvents.get(eventId);
        if (event != null) {
            activeEvents.add(eventId);
            event.onStart(server);
            setDirty();
            return true;
        }
        return false;
    }

    public boolean stopEvent(ResourceLocation eventId, ServerLevel server) {
        if (activeEvents.remove(eventId)) {
            GameplayEvent event = GameplayEvents.get(eventId);
            if (event != null) {
                event.onStop(server);
            }

            setDirty();
            return true;
        }
        return false;
    }
}