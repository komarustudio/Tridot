package pro.komaru.tridot.api.networking;

import net.minecraft.core.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.network.event.*;
import net.neoforged.neoforge.network.registration.*;
import net.neoforged.neoforge.server.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.common.networking.AbstractPacketHandler;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.stash.net.SyncStashObjectPacket;

import java.util.function.*;

public class PacketHandler extends AbstractPacketHandler {
    public static final String PROTOCOL = "11";
    private static final Seq<Consumer<PayloadRegistrar>> EXTRA = Seq.with();

    public static void addRegistration(Consumer<PayloadRegistrar> registration){
        EXTRA.add(registration);
    }

    public static void register(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = registrar(event, Tridot.ID, PROTOCOL);
        registrar.playToClient(DashParticlePacket.TYPE, DashParticlePacket.STREAM_CODEC, DashParticlePacket::handle);
        registrar.playToClient(CooldownSoundPacket.TYPE, CooldownSoundPacket.STREAM_CODEC, CooldownSoundPacket::handle);
        registrar.playToClient(DungeonSoundPacket.TYPE, DungeonSoundPacket.STREAM_CODEC, DungeonSoundPacket::handle);
        registrar.playToClient(UpdateBossbarPacket.TYPE, UpdateBossbarPacket.STREAM_CODEC, UpdateBossbarPacket::handle);
        registrar.playToClient(SynchronizeCapabilityPacket.TYPE, SynchronizeCapabilityPacket.STREAM_CODEC, Packet::handle);
        registrar.playToClient(SyncStashObjectPacket.TYPE, SyncStashObjectPacket.STREAM_CODEC, Packet::handle);
        registrar.playToServer(CutsceneSkippedPacket.TYPE, CutsceneSkippedPacket.STREAM_CODEC, CutsceneSkippedPacket::handle);
        registrar.playToClient(ParryParticlePacket.TYPE, ParryParticlePacket.STREAM_CODEC, ParryParticlePacket::handle);
        EXTRA.each(c -> c.accept(registrar));
    }

    public static void sendTo(ServerPlayer playerMP, CustomPacketPayload toSend){
        AbstractPacketHandler.sendTo(playerMP, toSend);
    }

    public static void sendToAll(CustomPacketPayload message){
        for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()){
            sendNonLocal(message, player);
        }
    }

    public static void sendNonLocal(CustomPacketPayload msg, ServerPlayer player){
        AbstractPacketHandler.sendTo(player, msg);
    }

    public static void sendNonLocal(ServerPlayer playerMP, CustomPacketPayload toSend){
        AbstractPacketHandler.sendNonLocal(playerMP, toSend);
    }

    public static void sendToTracking(Level world, BlockPos pos, CustomPacketPayload msg){
        AbstractPacketHandler.sendToTracking(world, pos, msg);
    }

    public static void sendTo(Player entity, CustomPacketPayload msg){
        AbstractPacketHandler.sendTo(entity, msg);
    }

    public static void sendEntity(Player entity, CustomPacketPayload msg){
        AbstractPacketHandler.sendEntity(entity, msg);
    }

    public static void sendToServer(CustomPacketPayload msg){
        AbstractPacketHandler.sendToServer(msg);
    }
}