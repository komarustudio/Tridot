package pro.komaru.tridot.client.networking;

import net.minecraft.client.*;
import net.minecraft.client.sounds.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.behavior.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.render.gui.overlay.*;
import pro.komaru.tridot.client.render.screenshake.*;
import pro.komaru.tridot.client.sound.*;
import pro.komaru.tridot.common.networking.packets.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.math.*;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {

    public static void playCooldownSound(SoundEvent event) {
        if (event == null) return;
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (TridotLibClient.COOLDOWN_SOUND != null && soundManager.isActive(TridotLibClient.COOLDOWN_SOUND)) {
            return;
        }

        TridotLibClient.COOLDOWN_SOUND = new TridotSoundInstance(event, Minecraft.getInstance().player);
        soundManager.play(TridotLibClient.COOLDOWN_SOUND);
        if (!soundManager.isActive(TridotLibClient.COOLDOWN_SOUND)) {
            TridotLibClient.COOLDOWN_SOUND = null;
        }
    }

    public static void handleDungeonSound(DungeonSoundPacket msg) {
        SoundEvent event = msg.getSoundEvent();
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (TridotLibClient.DUNGEON_MUSIC_INSTANCE != null && soundManager.isActive(TridotLibClient.DUNGEON_MUSIC_INSTANCE)) {
            return;
        }

        TridotLibClient.DUNGEON_MUSIC_INSTANCE = new TridotSoundInstance(event, SoundSource.MUSIC, Minecraft.getInstance().player);
        soundManager.play(TridotLibClient.DUNGEON_MUSIC_INSTANCE);
        if (!soundManager.isActive(TridotLibClient.DUNGEON_MUSIC_INSTANCE)) {
            TridotLibClient.DUNGEON_MUSIC_INSTANCE = null;
        }
    }

    public static void handleUpdateBossbar(UpdateBossbarPacket msg) {
        BossBarsOverlay.INSTANCE.update(msg);
    }

    public static void handleParryParticle(ParryParticlePacket msg) {
        Level level = Tridot.PROXY.getLevel();

        ParticleBuilder.create(TridotParticles.SQUARE)
                .setBehavior(SparkParticleBehavior.create().build())
                .setScaleData(GenericParticleData.create(0.00125f, 0.02f, 0).setEasing(Interp.bounce).build())
                .setLifetime(25)
                .setColorData(ColorParticleData.create(Col.white, Col.yellow).setEasing(Interp.bounce).build())
                .randomVelocity(0.125, 0.25, 0.125)
                .setHasPhysics(false)
                .repeat(level, msg.getPosX(), msg.getPosY(), msg.getPosZ(), 12);
        ScreenshakeHandler.add(new PositionedScreenshakeInstance(20, Pos3.init((float) msg.getPosX(), (float) msg.getPosY(), (float) msg.getPosZ()), 0, 3, Interp.elastic).interp(Interp.fade).intensity(2));
        OverlayHandler.addInstance(new TimedOverlayInstance().setTexture(Tridot.ofTridot("textures/gui/overlay/flash.png")).setShowTime(10).setOpacity(0.25f).setFadeIn(0));
    }
}
