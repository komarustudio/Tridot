package pro.komaru.tridot.common.networking.packets;

import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.networking.*;
import pro.komaru.tridot.client.render.screenshake.*;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.math.*;

public class ScreenshakePacket implements Packet {
    public static final Type<ScreenshakePacket> TYPE = Packet.type(Tridot.ID, "screenshake");
    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenshakePacket> STREAM_CODEC = Packet.codec(ScreenshakePacket::decode);

    private final boolean positioned;
    private final float x, y, z;
    private final float falloffDistance;
    private final float maxDistance;
    private final String falloffEasing;

    private final int duration;
    private final float intensity1, intensity2, intensity3;
    private final String startEasing;
    private final String endEasing;
    private final boolean isNormalize;
    private final boolean isRotation;
    private final boolean isPosition;
    private final boolean isVector;
    private final boolean isFov;
    private final boolean isFovNormalize;
    private final float vx, vy, vz;

    public ScreenshakePacket(ScreenshakeInstance instance) {
        if (instance instanceof PositionedScreenshakeInstance pos) {
            this.positioned = true;
            this.x = pos.position.x;
            this.y = pos.position.y;
            this.z = pos.position.z;
            this.falloffDistance = pos.falloffDistance;
            this.maxDistance = pos.maxDistance;
            this.falloffEasing = Interp.invTable.getOrDefault(pos.falloffEasing, "linear");
        } else {
            this.positioned = false;
            this.x = 0; this.y = 0; this.z = 0;
            this.falloffDistance = 0;
            this.maxDistance = 0;
            this.falloffEasing = "linear";
        }

        this.duration = instance.duration;
        this.intensity1 = instance.intensity1;
        this.intensity2 = instance.intensity2;
        this.intensity3 = instance.intensity3;
        this.startEasing = Interp.invTable.getOrDefault(instance.intensityCurveStartEasing, "linear");
        this.endEasing = Interp.invTable.getOrDefault(instance.intensityCurveEndEasing, "linear");
        this.isNormalize = instance.isNormalize;
        this.isRotation = instance.isRotation;
        this.isPosition = instance.isPosition;
        this.isVector = instance.isVector;
        this.isFov = instance.isFov;
        this.isFovNormalize = instance.isFovNormalize;
        Vec3 v = instance.vector != null ? instance.vector : Vec3.ZERO;
        this.vx = (float) v.x;
        this.vy = (float) v.y;
        this.vz = (float) v.z;
    }

    public ScreenshakePacket(boolean positioned, float x, float y, float z, float falloffDistance, float maxDistance, String falloffEasing, int duration, float intensity1, float intensity2, float intensity3, String startEasing, String endEasing, boolean isNormalize, boolean isRotation, boolean isPosition, boolean isVector, boolean isFov, boolean isFovNormalize, float vx, float vy, float vz) {
        this.positioned = positioned;
        this.x = x; this.y = y; this.z = z;
        this.falloffDistance = falloffDistance;
        this.maxDistance = maxDistance;
        this.falloffEasing = falloffEasing;
        this.duration = duration;
        this.intensity1 = intensity1;
        this.intensity2 = intensity2;
        this.intensity3 = intensity3;
        this.startEasing = startEasing;
        this.endEasing = endEasing;
        this.isNormalize = isNormalize;
        this.isRotation = isRotation;
        this.isPosition = isPosition;
        this.isVector = isVector;
        this.isFov = isFov;
        this.isFovNormalize = isFovNormalize;
        this.vx = vx; this.vy = vy; this.vz = vz;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeBoolean(positioned);
        if (positioned) {
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(falloffDistance);
            buf.writeFloat(maxDistance);
            buf.writeUtf(falloffEasing != null ? falloffEasing : "linear");
        }

        buf.writeInt(duration);
        buf.writeFloat(intensity1);
        buf.writeFloat(intensity2);
        buf.writeFloat(intensity3);
        buf.writeUtf(startEasing != null ? startEasing : "linear");
        buf.writeUtf(endEasing != null ? endEasing : "linear");
        buf.writeBoolean(isNormalize);
        buf.writeBoolean(isRotation);
        buf.writeBoolean(isPosition);
        buf.writeBoolean(isVector);
        buf.writeBoolean(isFov);
        buf.writeBoolean(isFovNormalize);
        buf.writeFloat(vx);
        buf.writeFloat(vy);
        buf.writeFloat(vz);
    }

    public static ScreenshakePacket decode(FriendlyByteBuf buf) {
        boolean positioned = buf.readBoolean();
        float x = 0, y = 0, z = 0, falloff = 0, maxDist = 0;
        String falloffEasing = "linear";
        if (positioned) {
            x = buf.readFloat();
            y = buf.readFloat();
            z = buf.readFloat();
            falloff = buf.readFloat();
            maxDist = buf.readFloat();
            falloffEasing = buf.readUtf();
        }
        int duration = buf.readInt();
        float i1 = buf.readFloat();
        float i2 = buf.readFloat();
        float i3 = buf.readFloat();
        String startEasing = buf.readUtf();
        String endEasing = buf.readUtf();
        boolean isNorm = buf.readBoolean();
        boolean isRot = buf.readBoolean();
        boolean isPos = buf.readBoolean();
        boolean isVec = buf.readBoolean();
        boolean isFov = buf.readBoolean();
        boolean isFovNorm = buf.readBoolean();
        float vx = buf.readFloat();
        float vy = buf.readFloat();
        float vz = buf.readFloat();

        return new ScreenshakePacket(positioned, x, y, z, falloff, maxDist, falloffEasing, duration, i1, i2, i3, startEasing, endEasing, isNorm, isRot, isPos, isVec, isFov, isFovNorm, vx, vy, vz);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doOnClient() {
        Interp startInterp = Interp.table.getOrDefault(startEasing, Interp.linear);
        Interp endInterp = Interp.table.getOrDefault(endEasing, Interp.linear);

        ScreenshakeInstance inst;
        if (positioned) {
            Interp fEasing = Interp.table.getOrDefault(falloffEasing, Interp.linear);
            inst = new PositionedScreenshakeInstance(duration, Pos3.init(x, y, z), falloffDistance, maxDistance, fEasing);
        } else {
            inst = new ScreenshakeInstance(duration);
        }

        inst.intensity(intensity1, intensity2, intensity3)
            .interp(startInterp, endInterp)
            .normalize(isNormalize)
            .rot(isRotation)
            .pos(isPosition)
            .vec(isVector)
            .fov(isFov)
            .normalizeFov(isFovNormalize);

        if (isVector) {
            inst.vec(new Vec3(vx, vy, vz));
        }

        ScreenshakeHandler.add(inst);
    }
}
