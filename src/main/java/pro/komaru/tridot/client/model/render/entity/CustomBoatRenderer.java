package pro.komaru.tridot.client.model.render.entity;

import com.mojang.datafixers.util.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.vehicle.*;
import org.jetbrains.annotations.*;

public class CustomBoatRenderer extends BoatRenderer{
    private final ResourceLocation boatTexture;
    private ListModel<Boat> boatModel;

    public CustomBoatRenderer(EntityRendererProvider.Context context, ResourceLocation boatTexture, boolean chest, boolean raft){
        super(context, chest);
        this.boatTexture = boatTexture;
        createModels(chest, raft);
    }

    public CustomBoatRenderer(EntityRendererProvider.Context context, String modId, String texture, boolean chest, boolean raft){
        super(context, chest);
        String path = chest ? "textures/entity/chest_boat/" + texture + ".png" : "textures/entity/boat/" + texture + ".png";
        this.boatTexture = ResourceLocation.fromNamespaceAndPath(modId, path);
        createModels(chest, raft);
    }

    public void createModels(boolean chest, boolean raft){
        if(raft){
            this.boatModel = chest ? new ChestRaftModel(ChestRaftModel.createBodyModel().bakeRoot()) : new RaftModel(RaftModel.createBodyModel().bakeRoot());
        }else{
            this.boatModel = chest ? new ChestBoatModel(ChestBoatModel.createBodyModel().bakeRoot()) : new BoatModel(BoatModel.createBodyModel().bakeRoot());
        }
    }

    @Override
    @NotNull
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(@NotNull Boat boat){
        return Pair.of(boatTexture, boatModel);
    }
}
