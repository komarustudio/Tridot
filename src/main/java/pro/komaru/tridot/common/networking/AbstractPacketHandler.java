package pro.komaru.tridot.common.networking;

import net.minecraft.core.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.*;
import net.neoforged.neoforge.network.registration.*;

public abstract class AbstractPacketHandler{

    public static PayloadRegistrar registrar(RegisterPayloadHandlersEvent event, String modId, String version){
        return event.registrar(modId).versioned(version);
    }

    public static void sendTo(ServerPlayer playerMP, CustomPacketPayload toSend){
        PacketDistributor.sendToPlayer(playerMP, toSend);
    }

    public static void sendNonLocal(ServerPlayer playerMP, CustomPacketPayload toSend){
        if(playerMP.server.isDedicatedServer() || !playerMP.getGameProfile().getName().equals(playerMP.server.getLocalIp())){
            sendTo(playerMP, toSend);
        }
    }

    public static void sendToTracking(Level level, BlockPos pos, CustomPacketPayload msg){
        var chunkpos = new ChunkPos(pos);
        var players = ((ServerChunkCache)level.getChunkSource()).chunkMap.getPlayers(chunkpos, false);
        for(var player : players){
            if(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64){
                PacketDistributor.sendToPlayer(player, msg);
            }
        }
    }

    public static void sendTo(Player entity, CustomPacketPayload msg){
        PacketDistributor.sendToPlayer((ServerPlayer)entity, msg);
    }

    public static void sendEntity(Player entity, CustomPacketPayload msg){
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
    }

    public static void sendToServer(CustomPacketPayload msg){
        PacketDistributor.sendToServer(msg);
    }
}
