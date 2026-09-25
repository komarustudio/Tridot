package pro.komaru.tridot.client.gfx.postprocess;

import com.mojang.blaze3d.vertex.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import pro.komaru.tridot.util.*;

import javax.annotation.*;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public abstract class PostProcessInstanceData{
    public static final Minecraft minecraft = Minecraft.getInstance();
    public final List<PostProcessInstance> instances = new ArrayList<>(getMaxInstances());
    public final ShaderDataBuffer dataBuffer = new ShaderDataBuffer();

    public abstract int getMaxInstances();

    public abstract int getDataSizePerInstance();

    public void init(){
        dataBuffer.generate((long)getMaxInstances() * getDataSizePerInstance());
    }

    @Nullable
    public PostProcessInstance addInstance(PostProcessInstance instance){
        if(instances.size() >= getMaxInstances()){
            Log.warn("Failed to add shader instance to " + this + ": reached max instance count of " + getMaxInstances());
            return null;
        }
        instances.add(instance);
        return instance;
    }

    public void beforeProcess(PoseStack viewModelStack){
        for(int i = instances.size() - 1; i >= 0; i--){
            PostProcessInstance instance = instances.get(i);
            instance.update(minecraft.getTimer().getRealtimeDeltaTicks());
            if(instance.isRemoved()){
                instances.remove(i);
            }
        }
        if(instances.isEmpty()) return;
        float[] data = new float[instances.size() * getDataSizePerInstance()];
        for(int ins = 0; ins < instances.size(); ins++){
            PostProcessInstance instance = instances.get(ins);
            int offset = ins * getDataSizePerInstance();
            instance.writeDataToBuffer((index, d) -> {
                if(index >= getDataSizePerInstance() || index < 0)
                    throw new IndexOutOfBoundsException(index);
                data[offset + index] = d;
            });
        }

        dataBuffer.upload(data);
    }

    public void setDataBufferUniform(EffectInstance effectInstance, String bufferName, String countName){
        dataBuffer.apply(effectInstance, bufferName);
        effectInstance.safeGetUniform(countName).set(instances.size());
    }

    public boolean isEmpty(){
        return instances.isEmpty();
    }
}
