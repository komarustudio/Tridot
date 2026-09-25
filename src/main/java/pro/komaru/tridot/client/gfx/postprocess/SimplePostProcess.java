package pro.komaru.tridot.client.gfx.postprocess;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.*;
import net.neoforged.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public class SimplePostProcess extends PostProcess{
    public ResourceLocation resourceLocation;
    public boolean screen = false;
    public boolean window = false;
    public float priority = 0;

    public SimplePostProcess(ResourceLocation resourceLocation){
        this.resourceLocation = resourceLocation;
    }

    public SimplePostProcess enableScreen(){
        return setScreen(true);
    }

    public SimplePostProcess disableScreen(){
        return setScreen(false);
    }

    public SimplePostProcess setScreen(boolean screen){
        this.screen = screen;
        return this;
    }

    public SimplePostProcess enableWindow(){
        return setWindow(true);
    }

    public SimplePostProcess disableWindow(){
        return setWindow(false);
    }

    public SimplePostProcess setWindow(boolean window){
        this.window = window;
        return this;
    }

    public SimplePostProcess setPriority(float priority){
        this.priority = priority;
        return this;
    }

    @Override
    public ResourceLocation getPostChainLocation(){
        return resourceLocation;
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack){

    }

    @Override
    public void afterProcess(){

    }

    @Override
    public boolean isScreen(){
        return screen;
    }

    @Override
    public boolean isWindow(){
        return window;
    }

    @Override
    public float getPriority(){
        return priority;
    }
}
