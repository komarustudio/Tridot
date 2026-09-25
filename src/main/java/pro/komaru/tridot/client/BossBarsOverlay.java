package pro.komaru.tridot.client;

import com.google.common.collect.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.render.bossbars.*;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.mixin.client.*;
import pro.komaru.tridot.util.*;

import java.util.*;

public class BossBarsOverlay implements LayeredDraw.Layer {
    public static final BossBarsOverlay INSTANCE = new BossBarsOverlay();
    public static final Map<UUID, ClientBossbar> events = Maps.newLinkedHashMap();

    public void update(UpdateBossbarPacket pPacket) {
        pPacket.dispatch(new UpdateBossbarPacket.Handler() {
            public void add(UUID uuid, ResourceLocation type, ResourceLocation texture, Component pName, float health, float maxHealth, Col color, SoundEvent event, boolean pPlayBossMusic, boolean pDarkenScreen, boolean pCreateWorldFog, boolean isRainbow){
                ClientBossbar bossbar = ClientBossbarRegistry.create(type, uuid);
                bossbar.setHealth(health, maxHealth);
                this.updateName(bossbar, pName);
                this.updateStyle(bossbar, color);
                this.updateProperties(bossbar, type, texture, event, pDarkenScreen, pPlayBossMusic, pCreateWorldFog, isRainbow);
                events.put(uuid, bossbar);
            }

            public void remove(UUID uuid) {
                events.remove(uuid);
            }

            public void updateProgress(UUID uuid, float health, float maxHealth){
                ClientBossbar bossbar = events.get(uuid);
                bossbar.setHealth(health, maxHealth);
                float percent = (maxHealth > 0.0F) ? (health / maxHealth) : 0.0F;
                bossbar.setPercentage(percent);
            }

            public void updateName(UUID uuid, Component component) {
                ClientBossbar bossbar = events.get(uuid);
                this.updateName(bossbar, component);
            }

            public void updateStyle(UUID uuid, Col color){
                ClientBossbar bossbar = events.get(uuid);
                this.updateStyle(bossbar, color);
            }

            public void updateProperties(UUID uuid, ResourceLocation type, ResourceLocation texture, SoundEvent event, boolean darkenSky, boolean shouldPlayBossMusic, boolean createFog, boolean isRainbow, boolean aboutToDie){
                ClientBossbar bossbar = events.get(uuid);
                bossbar.setAboutToDie(aboutToDie);
                this.updateProperties(bossbar, type, texture, event, darkenSky, shouldPlayBossMusic, createFog, isRainbow);
            }

            public void updateName(ClientBossbar bossbar, Component component) {
                bossbar.setName(component);
            }

            public void updateStyle(ClientBossbar bossbar, Col color){
                bossbar.setColor(color);
            }

            public void updateProperties(ClientBossbar bossbar, ResourceLocation type, ResourceLocation texture, SoundEvent event, boolean darkenSky, boolean shouldPlayBossMusic, boolean createFog, boolean isRainbow){
                bossbar.setTexture(texture);
                bossbar.setType(type);
                bossbar.setDarkenScreen(darkenSky);
                bossbar.setBossMusic(event);
                bossbar.setPlayBossMusic(shouldPlayBossMusic);
                bossbar.setRainbow(isRainbow);
                bossbar.setCreateWorldFog(createFog);
            }
        });
    }

    public static void reset() {
        events.clear();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui && events.isEmpty()) return;
        PoseStack pose = pGuiGraphics.pose();
        pose.pushPose();

        int y = 0;
        int baseOffset = calculateBossBarsOffset(minecraft);
        int renderedCount = 0;
        for(ClientBossbar bossbar : events.values()){
            if(renderedCount >= ClientConfig.BOSSBARS_LIMIT.get()) return;
            bossbar.render(this, pGuiGraphics, baseOffset + y, minecraft);
            y += bossbar.increment;
            renderedCount++;
        }

        pose.popPose();
    }

    public static int calculateBossBarsOffset(Minecraft minecraft){
        var bossOverlay = minecraft.gui.getBossOverlay();
        Map<UUID, LerpingBossEvent> events = ((BossHealthOverlayAccessor)bossOverlay).getEvents();
        return events.size() * 20;
    }
}