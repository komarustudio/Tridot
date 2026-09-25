package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.client.compatibility.*;
import pro.komaru.tridot.client.render.LevelRenderHandler;

public class GenericParticleRenderType implements ParticleRenderType{
    public static final GenericParticleRenderType INSTANCE = new GenericParticleRenderType();

    @Override
    public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager){
        if(ShadersIntegration.shouldApply()) LevelRenderHandler.MATRIX4F = RenderSystem.getModelViewMatrix();
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }
}