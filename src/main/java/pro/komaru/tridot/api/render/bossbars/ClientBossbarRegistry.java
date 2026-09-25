package pro.komaru.tridot.api.render.bossbars;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.client.*;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientBossbarRegistry {
    private static final Map<ResourceLocation, Class<? extends ClientBossbar>> REGISTRY = new HashMap<>();

    {
        register(Tridot.ofTridot("generic"), ClientBossbar.class);
    }

    public static void register(ResourceLocation id, Class<? extends ClientBossbar> bossbarClass) {
        REGISTRY.put(id, bossbarClass);
    }

    public static ClientBossbar create(ResourceLocation id, UUID uuid) {
        Class<? extends ClientBossbar> bossbarClass = REGISTRY.get(id);
        if (bossbarClass != null) {
            try {
                return bossbarClass.getConstructor(UUID.class, Component.class).newInstance(uuid, Component.empty());
            } catch (Exception e) {
                Tridot.LOGGER.error("Could not instantiate bossbar class", e);
            }
        }

        return new ClientBossbar(uuid, Component.empty()){
            public void render(BossBarsOverlay overlay, GuiGraphics pGuiGraphics, int baseOffset, Minecraft mc){}
        };
    }
}