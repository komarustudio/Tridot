package pro.komaru.tridot.client.gfx.postprocess;

import com.google.common.collect.*;
import com.google.gson.*;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.shaders.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.*;
import org.joml.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.util.*;

import java.io.*;
import java.lang.Math;
import java.util.*;
import java.util.function.*;

import static com.mojang.blaze3d.platform.GlConst.GL_DRAW_FRAMEBUFFER;

@OnlyIn(Dist.CLIENT)
public abstract class PostProcess{
    public static final Minecraft minecraft = Minecraft.getInstance();

    // PORT NOTE: Minecraft.getFrameTime()/getDeltaFrameTime() were replaced by the DeltaTracker (see ClientTick.mcPartialTick).
    public static final Collection<Pair<String, Consumer<Uniform>>> COMMON_UNIFORMS = Lists.newArrayList(
    Pair.of("cameraPos", u -> u.set(new Vector3f(minecraft.gameRenderer.getMainCamera().getPosition().toVector3f()))),
    Pair.of("lookVector", u -> u.set(minecraft.gameRenderer.getMainCamera().getLookVector())),
    Pair.of("upVector", u -> u.set(minecraft.gameRenderer.getMainCamera().getUpVector())),
    Pair.of("leftVector", u -> u.set(minecraft.gameRenderer.getMainCamera().getLeftVector())),
    Pair.of("invViewMat", u -> {
        Matrix4f invertedViewMatrix = new Matrix4f(PostProcess.viewModelStack.last().pose());
        invertedViewMatrix.invert();
        u.set(invertedViewMatrix);
    }),
    Pair.of("invProjMat", u -> {
        Matrix4f invertedProjectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
        invertedProjectionMatrix.invert();
        u.set(invertedProjectionMatrix);
    }),
    Pair.of("nearPlaneDistance", u -> u.set(GameRenderer.PROJECTION_Z_NEAR)),
    Pair.of("farPlaneDistance", u -> u.set(minecraft.gameRenderer.getDepthFar())),
    Pair.of("fov", u -> u.set((float)Math.toRadians(minecraft.gameRenderer.getFov(minecraft.gameRenderer.getMainCamera(), ClientTick.mcPartialTick(), true)))),
    Pair.of("aspectRatio", u -> u.set((float)minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight()))
    );

    public static PoseStack viewModelStack;

    public PostChain postChain;
    public boolean effectActive;
    public RenderTarget tempDepthBuffer;
    public EffectInstance[] effects;
    public Collection<Pair<Uniform, Consumer<Uniform>>> defaultUniforms;

    public boolean initialized = false;
    public boolean isActive = true;
    public double time;

    public void init(){
        loadPostChain();
        if(postChain != null){
            tempDepthBuffer = postChain.getTempTarget("depthMain");

            defaultUniforms = new ArrayList<>();
            for(EffectInstance e : effects){
                for(Pair<String, Consumer<Uniform>> pair : COMMON_UNIFORMS){
                    Uniform u = e.getUniform(pair.getFirst());
                    if(u != null){
                        defaultUniforms.add(Pair.of(u, pair.getSecond()));
                    }
                }
            }
        }
        initialized = true;
    }

    public void tick(){

    }

    public void loadPostChain(){
        if(postChain != null){
            postChain.close();
            postChain = null;
        }

        try{
            ResourceLocation resourceLocation = getPostChainLocation();
            postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), resourceLocation);
            postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            effects = postChain.passes.stream().map(PostPass::getEffect).toArray(EffectInstance[]::new);
            effectActive = true;
        }catch(IOException | JsonSyntaxException ioexception){
            Log.error("Failed to load post-processing shader: ", ioexception);
            effectActive = false;
        }
    }

    public void copyDepthBuffer(){
        if(isActive){
            if(postChain == null || tempDepthBuffer == null) return;
            tempDepthBuffer.copyDepthFrom(minecraft.getMainRenderTarget());
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, minecraft.getMainRenderTarget().frameBufferId);
        }
    }

    public void resize(int width, int height){
        if(postChain != null){
            postChain.resize(width, height);
            if(tempDepthBuffer != null)
                tempDepthBuffer.resize(width, height, Minecraft.ON_OSX);
        }
    }

    public void applyDefaultUniforms(){
        Arrays.stream(effects).forEach(e -> e.safeGetUniform("totalTicks").set(ClientTick.getTotal()));
        Arrays.stream(effects).forEach(e -> e.safeGetUniform("partialTicks").set(ClientTick.partialTicks));
        Arrays.stream(effects).forEach(e -> e.safeGetUniform("gameTicks").set(ClientTick.ticksInGame));
        Arrays.stream(effects).forEach(e -> e.safeGetUniform("time").set((float)time));
        defaultUniforms.forEach(pair -> pair.getSecond().accept(pair.getFirst()));
    }

    public void applyPostProcess(){
        if(isActive){
            if(!initialized) init();
            if(postChain != null){
                time += minecraft.getTimer().getRealtimeDeltaTicks() / 20.0;
                beforeProcess(viewModelStack);
                applyDefaultUniforms();
                if(!isActive) return;
                postChain.process(ClientTick.mcPartialTick());
                GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, minecraft.getMainRenderTarget().frameBufferId);
                afterProcess();
            }
        }
    }

    public abstract ResourceLocation getPostChainLocation();

    public abstract void beforeProcess(PoseStack viewModelStack);

    public abstract void afterProcess();

    public void setActive(boolean active){
        this.isActive = active;
        if(!active) time = 0.0;
    }

    public boolean isActive(){
        return isActive;
    }

    public boolean isScreen(){
        return false;
    }

    public boolean isWindow(){
        return false;
    }

    public float getPriority(){
        return 0;
    }
}
