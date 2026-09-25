package pro.komaru.tridot.client.gfx.postprocess;

import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class PostProcessHandler{
    public static final List<PostProcess> instances = new ArrayList<>();
    public static boolean didCopyDepth = false;

    public static void addInstance(PostProcess instance){
        instances.add(instance);
    }

    public static List<PostProcess> getInstances(){
        return instances;
    }

    public static List<PostProcess> getSortedInstances(){
        List<PostProcess> sortedInstances = new ArrayList<>(instances);
        sortedInstances.sort((i1, i2) -> Float.compare(i1.getPriority(), i2.getPriority()));
        return sortedInstances;
    }

    public static void copyDepthBuffer(){
        if(didCopyDepth) return;
        instances.forEach(PostProcess::copyDepthBuffer);
        didCopyDepth = true;
    }

    public static void resize(int width, int height){
        instances.forEach(i -> i.resize(width, height));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelRender(RenderLevelStageEvent event){
        if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS)){
            PostProcess.viewModelStack = event.getPoseStack();
        }
        if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_LEVEL)){
            copyDepthBuffer();
            for(PostProcess postProcess : getSortedInstances()){
                if(!postProcess.isScreen()){
                    postProcess.applyPostProcess();
                }
            }
            didCopyDepth = false;
        }
    }

    public static void onScreenRender(GameRenderer gameRenderer, float partialTicks, long nanoTime, boolean renderLevel){
        for(PostProcess postProcess : getSortedInstances()){
            if(postProcess.isScreen() && !postProcess.isWindow()){
                postProcess.applyPostProcess();
            }
        }
    }

    public static void onWindowRender(GameRenderer gameRenderer, float partialTicks, long nanoTime, boolean renderLevel){
        for(PostProcess postProcess : getSortedInstances()){
            if(postProcess.isScreen() && postProcess.isWindow()){
                postProcess.applyPostProcess();
            }
        }
    }

    public static void tick(){
        for(PostProcess postProcess : getSortedInstances()){
            postProcess.tick();
        }
    }
}
