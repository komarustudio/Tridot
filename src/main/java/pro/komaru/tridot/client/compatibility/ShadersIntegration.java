package pro.komaru.tridot.client.compatibility;

import net.irisshaders.iris.*;
import net.minecraft.client.*;
import net.neoforged.fml.*;

public class ShadersIntegration{
    public static boolean LOADED;

    public static class LoadedOnly{
        public static boolean isShadersEnabled(){
            return Iris.getIrisConfig().areShadersEnabled();
        }
    }

    public static void init(){
        LOADED = ModList.get().isLoaded("iris");
    }

    public static boolean isLoaded(){
        return LOADED;
    }

    public static boolean isShadersEnabled(){
        if(isLoaded()){
            return LoadedOnly.isShadersEnabled();
        }
        return false;
    }

    public static boolean shouldApply(){
        return isShadersEnabled() || Minecraft.useShaderTransparency();
    }
}
