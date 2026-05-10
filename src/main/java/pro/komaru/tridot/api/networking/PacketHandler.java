package pro.komaru.tridot.api.networking;

import com.mojang.datafixers.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.*;
import net.minecraftforge.server.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.common.networking.AbstractPacketHandler;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.struct.stash.net.SyncStashObjectPacket;

public class PacketHandler extends AbstractPacketHandler {
    public static final String PROTOCOL = "10";
    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(Tridot.ID, "network"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void init(){
        int id = 0;
        HANDLER.registerMessage(id++, DashParticlePacket.class, DashParticlePacket::encode, DashParticlePacket::decode, DashParticlePacket::handle);
        HANDLER.registerMessage(id++, CooldownSoundPacket.class, CooldownSoundPacket::encode, CooldownSoundPacket::decode, CooldownSoundPacket::handle);
        HANDLER.registerMessage(id++, DungeonSoundPacket.class, DungeonSoundPacket::encode, DungeonSoundPacket::decode, DungeonSoundPacket::handle);
        HANDLER.registerMessage(id++, UpdateBossbarPacket.class, UpdateBossbarPacket::encode, UpdateBossbarPacket::decode, UpdateBossbarPacket::handle);
        HANDLER.registerMessage(id++, SynchronizeCapabilityPacket.class, SynchronizeCapabilityPacket::save, SynchronizeCapabilityPacket::new, SynchronizeCapabilityPacket::handle);
        HANDLER.registerMessage(id++, SyncStashObjectPacket.class, SyncStashObjectPacket::save, SyncStashObjectPacket::new, SyncStashObjectPacket::handle);
        HANDLER.registerMessage(id++, CutsceneSkippedPacket.class, CutsceneSkippedPacket::encode, CutsceneSkippedPacket::decode, CutsceneSkippedPacket::handle);
    }

    public static SimpleChannel getHandler(){
        return HANDLER;
    }

    public static void sendTo(ServerPlayer playerMP, Object toSend){
        HANDLER.sendTo(toSend, playerMP.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object message){
        for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()){
            sendNonLocal(message, player);
        }
    }

    public static void sendNonLocal(Object msg, ServerPlayer player){
        HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendNonLocal(ServerPlayer playerMP, Object toSend){
        if(playerMP.server.isDedicatedServer() || !playerMP.getGameProfile().getName().equals(playerMP.server.getLocalIp())){
            sendTo(playerMP, toSend);
        }
    }

    public static void sendToTracking(Level world, BlockPos pos, Object msg){
        HANDLER.send(TRACKING_CHUNK_AND_NEAR.with(() -> Pair.of(world, pos)), msg);
    }

    public static void sendTo(Player entity, Object msg){
        HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)entity), msg);
    }

    public static void sendEntity(Player entity, Object msg){
        HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    public static void sendToServer(Object msg){
        HANDLER.sendToServer(msg);
    }
}