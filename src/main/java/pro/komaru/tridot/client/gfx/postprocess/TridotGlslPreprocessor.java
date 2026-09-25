package pro.komaru.tridot.client.gfx.postprocess;

import com.mojang.blaze3d.preprocessor.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import net.minecraft.client.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import org.apache.commons.io.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.util.*;

import java.io.*;
import java.nio.charset.*;

@OnlyIn(Dist.CLIENT)
public class TridotGlslPreprocessor extends GlslPreprocessor{

    public static final TridotGlslPreprocessor PREPROCESSOR = new TridotGlslPreprocessor();

    @Nullable
    @Override
    public String applyImport(boolean useFullPath, String directory){
        ResourceLocation resourcelocation = ResourceLocation.parse(directory);
        ResourceLocation resourcelocation1 = ResourceLocation.fromNamespaceAndPath(resourcelocation.getNamespace(), "shaders/include/" + resourcelocation.getPath());
        try{
            Resource resource1 = Minecraft.getInstance().getResourceManager().getResource(resourcelocation1).get();
            return IOUtils.toString(resource1.open(), StandardCharsets.UTF_8);
        }catch(IOException ioexception){
            Log.error("Could not open GLSL import {}: {}", directory, ioexception.getMessage());
            return "#error " + ioexception.getMessage();
        }
    }
}
