package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;

public class GenericParticleRenderType implements ParticleRenderType{
    public static final GenericParticleRenderType INSTANCE = new GenericParticleRenderType();

    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager){
    }

    @Override
    public void end(Tesselator tesselator){

    }
}
