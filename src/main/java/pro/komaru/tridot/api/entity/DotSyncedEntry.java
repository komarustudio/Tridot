package pro.komaru.tridot.api.entity;

import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.struct.func.Cons2;
import pro.komaru.tridot.util.struct.func.Func;

import java.lang.reflect.*;

public class DotSyncedEntry<T> {
    /** PORT NOTE: synched data is defined through SynchedEntityData.Builder in 1.21, so {@code define} consumes the builder. */
    public Cons<SynchedEntityData.Builder> define;
    public Cons2<SynchedEntityData,T> set;
    public Func<SynchedEntityData,T> get;

    public EntityDataAccessor<T> accessor;

    public Class<T> type;

    public String name;

    @SuppressWarnings("unchecked")
    public DotSyncedEntry(String name, Class<? extends LivingEntity> entity, T def) {
        type = (Class<T>) def.getClass();
        accessor = SynchedEntityData.defineId(entity, getSerializer());
        define = builder -> builder.define(accessor,def);
        set = (data,v) -> data.set(accessor,v);
        get = (data) -> data.get(accessor);
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public EntityDataSerializer<T> getSerializer() {
        for (Field field : EntityDataSerializers.class.getFields()) {
            if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
                Type actualType = parameterizedType.getActualTypeArguments()[0]; // first generic argument
                if (actualType.equals(type)) { // compare with our type
                    try {
                        return (EntityDataSerializer<T>) field.get(null); // read the static field
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
