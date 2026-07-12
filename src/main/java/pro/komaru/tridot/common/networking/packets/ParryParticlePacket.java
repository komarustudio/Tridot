package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.client.gfx.TridotParticles;
import pro.komaru.tridot.client.gfx.particle.ParticleBuilder;
import pro.komaru.tridot.client.gfx.particle.behavior.SparkParticleBehavior;
import pro.komaru.tridot.client.gfx.particle.data.ColorParticleData;
import pro.komaru.tridot.client.gfx.particle.data.GenericParticleData;
import pro.komaru.tridot.client.render.gui.overlay.OverlayHandler;
import pro.komaru.tridot.client.render.gui.overlay.TimedOverlayInstance;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.math.Interp;

import java.util.function.Supplier;

public class ParryParticlePacket {
    private final double posX, posY, posZ;

    public ParryParticlePacket(double posX, double posY, double posZ){
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static ParryParticlePacket decode(FriendlyByteBuf buf){
        return new ParryParticlePacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ParryParticlePacket msg, Supplier<Context> ctx){
        if(ctx.get().getDirection().getReceptionSide().isClient()){
            ctx.get().enqueueWork(() -> {
                Level level = ctx.get().getSender().level();

                ParticleBuilder.create(TridotParticles.SQUARE)
                .setBehavior(SparkParticleBehavior.create().build())
                .setScaleData(GenericParticleData.create(0.00125f, 0.02f, 0).setEasing(Interp.bounce).build())
                .setLifetime(25)
                .setColorData(ColorParticleData.create(Col.white, Col.yellow).setEasing(Interp.bounce).build())
                .randomVelocity(0.125, 0.25, 0.125)
                .setHasPhysics(false)
                .repeat(level, msg.posX, msg.posY, msg.posZ, 12);

                OverlayHandler.addInstance(new TimedOverlayInstance().setTexture(new ResourceLocation(Tridot.ID, "textures/gui/overlay/flash.png")).setShowTime(10).setOpacity(0.25f).setFadeIn(0));
                ctx.get().setPacketHandled(true);
            });
        }
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }
}
