package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.network.handling.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.behavior.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.render.gui.overlay.*;
import pro.komaru.tridot.client.render.screenshake.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.math.*;

public class ParryParticlePacket implements CustomPacketPayload{
    public static final Type<ParryParticlePacket> TYPE = Packet.type(Tridot.ID, "parry_particle");
    public static final StreamCodec<RegistryFriendlyByteBuf, ParryParticlePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> p.encode(buf), ParryParticlePacket::decode);

    private final double posX, posY, posZ;

    public ParryParticlePacket(double posX, double posY, double posZ){
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static ParryParticlePacket decode(FriendlyByteBuf buf){
        return new ParryParticlePacket(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ParryParticlePacket msg, IPayloadContext ctx){
        if(ctx.flow().isClientbound()){
            ctx.enqueueWork(() -> {
                Level level = Tridot.PROXY.getLevel();

                ParticleBuilder.create(TridotParticles.SQUARE)
                .setBehavior(SparkParticleBehavior.create().build())
                .setScaleData(GenericParticleData.create(0.00125f, 0.02f, 0).setEasing(Interp.bounce).build())
                .setLifetime(25)
                .setColorData(ColorParticleData.create(Col.white, Col.yellow).setEasing(Interp.bounce).build())
                .randomVelocity(0.125, 0.25, 0.125)
                .setHasPhysics(false)
                .repeat(level, msg.posX, msg.posY, msg.posZ, 12);
                ScreenshakeHandler.add(new PositionedScreenshakeInstance(20, Pos3.init((float) msg.posX, (float) msg.posY, (float) msg.posZ), 0, 3, Interp.elastic).interp(Interp.fade).intensity(2));
                OverlayHandler.addInstance(new TimedOverlayInstance().setTexture(Tridot.ofTridot("textures/gui/overlay/flash.png")).setShowTime(10).setOpacity(0.25f).setFadeIn(0));
            });
        }
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
