package pro.komaru.tridot.api.entity;

import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.func.Cons2;

public interface DotSynced {
    Seq<DotSyncedEntry<?>> entries();
    SynchedEntityData synchedData();

    default <T> DotSyncedEntry<T> getVar(String name) {
        return (DotSyncedEntry<T>) entries().find(e -> e.name.equals(name));
    }

    default <T> T get(String name) {
        return (T) getVar(name).get.get(synchedData());
    }
    default <T> void set(String name, T val) {
        getVar(name).set.get(synchedData(),val);
    }

    default void writeData(CompoundTag tag) {
        entries().each(e -> {
            Object obj = e.get.get(synchedData());
            if (e.type == Integer.class) {
                tag.putInt(e.name, (Integer) obj);
            } else if (e.type == Float.class) {
                tag.putFloat(e.name, (Float) obj);
            } else if (e.type == String.class) {
                tag.putString(e.name, (String) obj);
            } else if (e.type == Boolean.class) {
                tag.putBoolean(e.name, (Boolean) obj);
            }
        });
    }
    default void readData(CompoundTag tag) {
        entries().each(e -> {
            Object value = null;
            Cons2<SynchedEntityData,Object> set = (Cons2<SynchedEntityData, Object>) e.set;
            if (e.type == Integer.class) {
                value = tag.getInt(e.name);
            } else if (e.type == Float.class) {
                value = tag.getFloat(e.name);
            } else if (e.type == String.class) {
                value = tag.getString(e.name);
            } else if (e.type == Boolean.class) {
                value = tag.getBoolean(e.name);
            }

            if (value != null) {
                set.get(synchedData(), value);  // This is safe since `value` is now typed correctly.
            }
        });
    }
    default void defineData(SynchedEntityData.Builder builder) {
        entries().each(e -> e.define.get(builder));
    }
}
