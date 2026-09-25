package pro.komaru.tridot.common.registry.block;

import com.mojang.blaze3d.shaders.*;
import com.mojang.blaze3d.systems.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.*;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.fluids.*;
import org.jetbrains.annotations.*;
import org.joml.*;

import java.util.function.*;

public class CustomFluidType extends FluidType{
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;
    private final int tintColor;
    private final Vector3f fogColor;

    public CustomFluidType(final ResourceLocation stillTexture, final ResourceLocation flowingTexture, final ResourceLocation overlayTexture, final int tintColor, final Vector3f fogColor, final Properties properties){
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor;
        this.fogColor = fogColor;
    }

    public ResourceLocation getStillTexture(){
        return stillTexture;
    }

    public ResourceLocation getFlowingTexture(){
        return flowingTexture;
    }

    public int getTintColor(){
        return tintColor;
    }

    public ResourceLocation getOverlayTexture(){
        return overlayTexture;
    }

    public Vector3f getFogColor(){
        return fogColor;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer){
        consumer.accept(new IClientFluidTypeExtensions(){
            @Override
            public ResourceLocation getStillTexture(){
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture(){
                return flowingTexture;
            }

            @Override
            public @Nullable ResourceLocation getOverlayTexture(){
                return overlayTexture;
            }

            @Override
            public int getTintColor(){
                return tintColor;
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor){
                return fogColor;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                        float nearDistance, float farDistance, FogShape shape){
                RenderSystem.setShaderFogStart(1f);
                RenderSystem.setShaderFogEnd(6f);
            }
        });
    }
}