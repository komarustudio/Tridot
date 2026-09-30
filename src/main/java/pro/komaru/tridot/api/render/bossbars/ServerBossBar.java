package pro.komaru.tridot.api.render.bossbars;

import com.google.common.collect.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.*;

import java.util.*;
import java.util.function.*;

public class ServerBossBar extends TridotBossBar{
    private String id;
    private final Set<ServerPlayer> players = Sets.newHashSet();
    private final Set<ServerPlayer> unmodifiablePlayers;
    private boolean visible = true;

    public ServerBossBar(Component pName, ResourceLocation type){
        super(Mth.createInsecureUUID(), pName);
        this.setType(type);
        this.unmodifiablePlayers = Collections.unmodifiableSet(this.players);
    }

    public ServerBossBar setType(ResourceLocation typeId) {
        if (!Objects.equals(this.clientBossbarType, typeId)) {
            super.setType(typeId);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setHealth(float health, float maxHealth){
        if (health != this.health || maxHealth != this.maxHealth || health / maxHealth != this.percentage) {
            super.setHealth(health, maxHealth);
            this.broadcast(UpdateBossbarPacket::createUpdateProgressPacket);
        }

        return this;
    }

    public ServerBossBar setRainbow(boolean rainbow){
        if (rainbow != this.rainbow) {
            super.setRainbow(rainbow);
            this.broadcast(UpdateBossbarPacket::createUpdateStylePacket);
        }

        return this;
    }

    public ServerBossBar setColor(Col pColor) {
        if (pColor != this.color) {
            super.setColor(pColor);
            this.broadcast(UpdateBossbarPacket::createUpdateStylePacket);
        }

        return this;
    }

    public ServerBossBar setAboutToDie(boolean aboutToDie){
        if (aboutToDie != this.aboutToDie) {
            super.setAboutToDie(aboutToDie);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setDarkenScreen(boolean pDarkenSky) {
        if (pDarkenSky != this.darkenScreen) {
            super.setDarkenScreen(pDarkenSky);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setName(Component pName) {
        if (!Objects.equals(pName, this.name)) {
            super.setName(pName);
            PacketHandler.sendToAll(UpdateBossbarPacket.createUpdateNamePacket(this));
        }

        return this;
    }

    public ServerBossBar setBossMusic(SoundEvent music){
        if (music != this.bossMusic) {
            super.setBossMusic(music);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setTexture(ResourceLocation texture){
        if (texture != this.texture) {
            super.setTexture(texture);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setPlayBossMusic(boolean playBossMusic) {
        if (playBossMusic != this.playBossMusic) {
            super.setPlayBossMusic(playBossMusic);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public ServerBossBar setCreateWorldFog(boolean pCreateFog) {
        if (pCreateFog != this.createWorldFog) {
            super.setCreateWorldFog(pCreateFog);
            this.broadcast(UpdateBossbarPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    public void setId(String id){
        if(!Objects.equals(id, this.id)){
            this.id = id;
            PacketHandler.sendToAll(UpdateBossbarPacket.createAddPacket(this));
        }
    }

    public void addPlayer(ServerPlayer serverPlayer){
        if (this.players.add(serverPlayer) && this.visible) {
            PacketHandler.sendNonLocal(UpdateBossbarPacket.createAddPacket(this), serverPlayer);
        }
    }

    public void removePlayer(ServerPlayer serverPlayer){
        if (this.players.remove(serverPlayer) && this.visible) {
            PacketHandler.sendNonLocal(UpdateBossbarPacket.createRemovePacket(this.getId()), serverPlayer);
        }
    }

    public void writeCustomData(FriendlyByteBuf buf) {

    }

    public void syncCustomData() {
        this.broadcast(UpdateBossbarPacket::createCustomDataPacket);
    }

    public void broadcast(Function<ServerBossBar, UpdateBossbarPacket> pPacketGetter) {
        if (this.visible) {
            UpdateBossbarPacket event = pPacketGetter.apply(this);
            for(ServerPlayer player : this.players) {
                PacketHandler.sendNonLocal(event, player);
            }
        }
    }

    public void removeAllPlayers() {
        if (!this.players.isEmpty()) {
            for(ServerPlayer players : Lists.newArrayList(this.players)) {
                this.removePlayer(players);
            }
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean pVisible) {
        if (pVisible != this.visible) {
            this.visible = pVisible;
            for(ServerPlayer serverPlayer : this.players) {
                PacketHandler.sendNonLocal(pVisible ? UpdateBossbarPacket.createAddPacket(this) : UpdateBossbarPacket.createRemovePacket(this.getId()), serverPlayer);
            }
        }
    }

    public Collection<ServerPlayer> getPlayers() {
        return this.unmodifiablePlayers;
    }
}