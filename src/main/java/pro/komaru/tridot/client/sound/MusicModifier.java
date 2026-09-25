package pro.komaru.tridot.client.sound;

import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.structure.*;
import pro.komaru.tridot.util.struct.func.Boolf;

public class MusicModifier{

    public boolean canPlay(Music defaultMusic, Minecraft minecraft){
        return true;
    }
    public Music play(Music defaultMusic, Minecraft minecraft){
        return null;
    }

    public boolean is(Boolf<Biome> biome, Minecraft minecraft){
        if(minecraft.player == null) return false;
        Holder<Biome> holder = minecraft.player.level().getBiome(minecraft.player.blockPosition());
        return biome.get(holder.value());
    }

    public static class DungeonMusic extends MusicModifier {
        public SoundEvent music;
        public ResourceKey<Structure> structureKey;

        public DungeonMusic(SoundEvent music, ResourceKey<Structure> structureKey){
            this.music = music;
            this.structureKey = structureKey;
        }

        public boolean isPlayerInStructure(Player player, ServerLevel serverLevel) {
            var structure = serverLevel.structureManager().getStructureWithPieceAt(player.blockPosition(), holder -> holder.is(structureKey));
            if(!structure.isValid()) return false;
            return structure.getBoundingBox().isInside(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        }
    }
}