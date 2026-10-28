package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix4f;
import pro.komaru.tridot.client.render.LevelRenderHandler;

public class GenericParticleRenderType implements ParticleRenderType{
    public static final GenericParticleRenderType INSTANCE = new GenericParticleRenderType();

    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager){
        LevelRenderHandler.MATRIX4F = new Matrix4f(RenderSystem.getModelViewMatrix());
    }

    @Override
    public void end(Tesselator tesselator){

    }
}