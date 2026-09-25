package pro.komaru.tridot.client.gfx.particle;

import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.neoforge.client.model.data.*;
import pro.komaru.tridot.client.gfx.particle.options.ItemParticleOptions;

public class ItemParticle extends GenericParticle{

    public ItemParticle(ClientLevel level, ItemParticleOptions options, double x, double y, double z, double vx, double vy, double vz){
        super(level, options, null, x, y, z, vx, vy, vz);
        var model = Minecraft.getInstance().getItemRenderer().getModel(options.stack, level, null, 0);
        this.setSprite(model.getOverrides().resolve(model, options.stack, level, null, 0).getParticleIcon(ModelData.EMPTY));
    }
}
