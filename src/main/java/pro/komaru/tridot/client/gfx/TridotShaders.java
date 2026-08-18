package pro.komaru.tridot.client.gfx;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.event.lifecycle.*;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.client.gfx.postprocess.GlowPostProcess;
import pro.komaru.tridot.client.gfx.postprocess.PostProcessHandler;

import java.io.*;

public class TridotShaders{
    public static ShaderInstance ADDITIVE_TEXTURE, ADDITIVE, TRANSLUCENT_TEXTURE, TRANSLUCENT, SCREEN_PARTICLE;

    @Mod.EventBusSubscriber(modid = Tridot.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistryEvents{
        @SubscribeEvent
        public static void registerShaders(FMLClientSetupEvent event){
            PostProcessHandler.addInstance(GlowPostProcess.INSTANCE);
        }

        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException{
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Tridot.ofTridot("additive_texture"), DefaultVertexFormat.POSITION_TEX_COLOR), shader -> ADDITIVE_TEXTURE = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Tridot.ofTridot("additive"), DefaultVertexFormat.POSITION_COLOR), shader -> ADDITIVE = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Tridot.ofTridot("translucent_texture"), DefaultVertexFormat.PARTICLE), shader -> TRANSLUCENT_TEXTURE = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Tridot.ofTridot("translucent"), DefaultVertexFormat.PARTICLE), shader -> TRANSLUCENT = shader);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Tridot.ofTridot("screen_particle"), DefaultVertexFormat.POSITION_TEX_COLOR), shader -> SCREEN_PARTICLE = shader);
        }
    }

    public static ShaderInstance getAdditiveTexture(){
        return ADDITIVE_TEXTURE;
    }

    public static ShaderInstance getAdditive(){
        return ADDITIVE;
    }

    public static ShaderInstance getTranslucentTexture(){
        return TRANSLUCENT_TEXTURE;
    }

    public static ShaderInstance getScreenParticle(){
        return SCREEN_PARTICLE;
    }

    public static ShaderInstance getTranslucent(){
        return TRANSLUCENT;
    }
}