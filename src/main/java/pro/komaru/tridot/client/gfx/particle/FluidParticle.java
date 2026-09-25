package pro.komaru.tridot.client.gfx.particle;

import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.level.material.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.fluids.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.client.gfx.particle.options.FluidParticleOptions;

public class FluidParticle extends GenericParticle{

    public FluidParticle(ClientLevel level, FluidParticleOptions options, double x, double y, double z, double vx, double vy, double vz){
        super(level, options, null, x, y, z, vx, vy, vz);
        if(!options.fluidStack.isEmpty()){
            FluidType type = options.fluidStack.getFluid().getFluidType();
            IClientFluidTypeExtensions clientType = IClientFluidTypeExtensions.of(type);
            TextureAtlasSprite sprite = Utils.Render.getSprite(clientType.getStillTexture(options.fluidStack));
            if(options.flowing) sprite = Utils.Render.getSprite(clientType.getFlowingTexture(options.fluidStack));
            this.setSprite(sprite);
        }else{
            IClientFluidTypeExtensions clientType = IClientFluidTypeExtensions.of(Fluids.WATER);
            TextureAtlasSprite sprite = Utils.Render.getSprite(clientType.getStillTexture());
            this.setSprite(sprite);
        }
    }
}
