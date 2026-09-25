package pro.komaru.tridot.api.capabilities;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.neoforged.neoforge.attachment.*;
import pro.komaru.tridot.util.struct.capability.CapImpl;
import pro.komaru.tridot.util.struct.func.Prov;

public class CapabilityEntry<T extends CapImpl> {
    public Prov<T> factory;
    public String modId;
    public String capId;
    public int id;
    private AttachmentType<T> type;

    public ResourceLocation location() {
        return ResourceLocation.fromNamespaceAndPath(modId, capId);
    }

    /** The registered attachment type; only valid after registry events have run. */
    public AttachmentType<T> type() {
        if (type == null) throw new IllegalStateException("Capability " + location() + " is not registered yet");
        return type;
    }

    AttachmentType<T> buildType() {
        type = AttachmentType.builder(holder -> factory.get())
            .serialize(new IAttachmentSerializer<CompoundTag, T>() {
                @Override
                public T read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    T value = factory.get();
                    value.deserializeNBT(provider, tag);
                    return value;
                }

                @Override
                public CompoundTag write(T attachment, HolderLookup.Provider provider) {
                    return attachment.serializeNBT(provider);
                }
            })
            .copyOnDeath()
            .build();
        return type;
    }
}
