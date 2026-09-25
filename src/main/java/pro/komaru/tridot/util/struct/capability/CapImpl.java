package pro.komaru.tridot.util.struct.capability;

import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.neoforged.neoforge.common.util.*;

public interface CapImpl extends INBTSerializable<CompoundTag> {
    void sync(ServerPlayer player);
}
