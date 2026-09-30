package pro.komaru.tridot.common.networking.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.sounds.*;
import net.minecraftforge.network.NetworkEvent.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.render.bossbars.*;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.util.*;

import java.util.*;
import java.util.function.*;

public record UpdateBossbarPacket(UUID id, Operation operation){
    static final Operation REMOVE_OPERATION = new Operation(){
        public OperationType getType(){
            return OperationType.REMOVE;
        }

        public void dispatch(UUID p_178660_, Handler p_178661_){
            p_178661_.remove(p_178660_);
        }

        public void encode(FriendlyByteBuf p_178663_){
        }
    };

    public static UpdateBossbarPacket decode(FriendlyByteBuf pBuffer){
        UUID id = pBuffer.readUUID();
        OperationType operationType = pBuffer.readEnum(OperationType.class);
        return new UpdateBossbarPacket(id, operationType.reader.apply(pBuffer));
    }

    public static UpdateBossbarPacket createAddPacket(TridotBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new AddOperation(pEvent));
    }

    public static UpdateBossbarPacket createRemovePacket(UUID pId){
        return new UpdateBossbarPacket(pId, REMOVE_OPERATION);
    }

    public static UpdateBossbarPacket createUpdateProgressPacket(TridotBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new UpdateProgressOperation(pEvent.getHealth(), pEvent.getMaxHealth()));
    }

    public static UpdateBossbarPacket createUpdateNamePacket(TridotBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new UpdateNameOperation(pEvent.getName()));
    }

    public static UpdateBossbarPacket createUpdateStylePacket(TridotBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new UpdateStyleOperation(pEvent.getColor()));
    }

    public static UpdateBossbarPacket createUpdatePropertiesPacket(TridotBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new UpdatePropertiesOperation(pEvent.getType(), pEvent.getTexture(), pEvent.getBossMusic(), pEvent.shouldDarkenScreen(), pEvent.shouldPlayBossMusic(), pEvent.shouldCreateWorldFog(), pEvent.isRainbow(), pEvent.isAboutToDie()));
    }

    public void encode(FriendlyByteBuf pBuffer){
        pBuffer.writeUUID(this.id);
        pBuffer.writeEnum(this.operation.getType());
        this.operation.encode(pBuffer);
    }

    public static void handle(UpdateBossbarPacket pMsg, Supplier<Context> context){
        context.get().setPacketHandled(true);
        BossBarsOverlay.INSTANCE.update(pMsg);
    }

    public void dispatch(Handler pHandler){
        this.operation.dispatch(this.id, pHandler);
    }

    public static UpdateBossbarPacket createCustomDataPacket(ServerBossBar pEvent){
        return new UpdateBossbarPacket(pEvent.getId(), new CustomDataOperation(pEvent));
    }

    enum OperationType{
        ADD(AddOperation::new),
        REMOVE((p_178719_) -> UpdateBossbarPacket.REMOVE_OPERATION),
        UPDATE_PROGRESS(UpdateProgressOperation::new),
        UPDATE_NAME(UpdateNameOperation::new),
        UPDATE_STYLE(UpdateStyleOperation::new),
        UPDATE_PROPERTIES(UpdatePropertiesOperation::new),
        UPDATE_CUSTOM_DATA(CustomDataOperation::new);

        final Function<FriendlyByteBuf, Operation> reader;

        OperationType(Function<FriendlyByteBuf, Operation> pReader){
            this.reader = pReader;
        }
    }

    public interface Handler{
        default void add(UUID uuid, ResourceLocation type, ResourceLocation texture, Component pName, float health, float maxHealth, Col color, SoundEvent event, boolean pPlayBossMusic, boolean pDarkenScreen, boolean pCreateWorldFog, boolean isRainbow){
        }

        default void remove(UUID uuid){
        }

        default void updateProgress(UUID uuid, float health, float maxHealth){
        }

        default void updateName(UUID uuid, Component component){
        }

        default void updateStyle(UUID uuid, Col color){
        }

        default void updateProperties(UUID uuid, ResourceLocation type, ResourceLocation texture, SoundEvent event, boolean darkenSky, boolean shouldPlayBossMusic, boolean createFog, boolean isRainbow, boolean aboutToDie){
        }

        default void updateCustomData(UUID uuid, FriendlyByteBuf buf) {
        }
    }

    interface Operation{
        OperationType getType();

        void dispatch(UUID pId, Handler pHandler);

        void encode(FriendlyByteBuf pBuffer);
    }

    static class CustomDataOperation implements Operation {
        private final Consumer<FriendlyByteBuf> writer;
        private FriendlyByteBuf receivedPayload;

        public CustomDataOperation(ServerBossBar event) {
            this.writer = event::writeCustomData;
        }

        public CustomDataOperation(FriendlyByteBuf pBuffer) {
            this.writer = null;
            int length = pBuffer.readVarInt();
            this.receivedPayload = new FriendlyByteBuf(pBuffer.readBytes(length));
        }

        @Override
        public OperationType getType() {
            return OperationType.UPDATE_CUSTOM_DATA;
        }

        @Override
        public void dispatch(UUID pId, Handler pHandler) {
            if (this.receivedPayload != null) {
                pHandler.updateCustomData(pId, this.receivedPayload);
                this.receivedPayload.release();
            }
        }

        @Override
        public void encode(FriendlyByteBuf pBuffer) {
            FriendlyByteBuf tempBuf = new FriendlyByteBuf(Unpooled.buffer());
            try {
                if (this.writer != null) {
                    this.writer.accept(tempBuf);
                }
                pBuffer.writeVarInt(tempBuf.readableBytes());
                pBuffer.writeBytes(tempBuf);
            } finally {
                tempBuf.release();
            }
        }
    }

    static class AddOperation implements Operation{
        public float health;
        public float maxHealth;
        public float r, g, b;
        public SoundEvent bossMusic;
        public boolean rainbow;
        public boolean createWorldFog;
        public boolean playBossMusic;
        public boolean darkenScreen;
        protected Component name;
        public ResourceLocation clientBossbarType;
        public ResourceLocation texture;

        public AddOperation(FriendlyByteBuf pBuffer){
            this(
            pBuffer.readResourceLocation(),
            pBuffer.readResourceLocation(),
            pBuffer.readComponent(),
            pBuffer.readFloat(),
            pBuffer.readFloat(),
            new Col(pBuffer.readFloat(), pBuffer.readFloat(), pBuffer.readFloat()),
            SoundEvent.createFixedRangeEvent(pBuffer.readResourceLocation(), 16),
            pBuffer.readBoolean(),
            pBuffer.readBoolean(),
            pBuffer.readBoolean(),
            pBuffer.readBoolean()
            );
        }

        public AddOperation(ResourceLocation type, ResourceLocation texture, Component name, float health, float maxHealth, Col color, SoundEvent bossMusic, boolean darkenScreen, boolean playBossMusic, boolean createWorldFog, boolean rainbow){
            this.clientBossbarType = type;
            this.texture = texture;
            this.name = name;
            this.health = health;
            this.maxHealth = maxHealth;
            this.r = color.r;
            this.g = color.g;
            this.b = color.b;
            this.bossMusic = bossMusic;
            this.darkenScreen = darkenScreen;
            this.playBossMusic = playBossMusic;
            this.createWorldFog = createWorldFog;
            this.rainbow = rainbow;
        }

        public AddOperation(TridotBossBar pEvent){
            this.texture = pEvent.getTexture();
            this.clientBossbarType = pEvent.getType();
            this.name = pEvent.getName();
            this.health = pEvent.getHealth();
            this.maxHealth = pEvent.getMaxHealth();
            this.r = pEvent.getColor().r;
            this.g = pEvent.getColor().g;
            this.b = pEvent.getColor().b;
            this.bossMusic = pEvent.getBossMusic();
            this.darkenScreen = pEvent.shouldDarkenScreen();
            this.playBossMusic = pEvent.shouldPlayBossMusic();
            this.createWorldFog = pEvent.shouldCreateWorldFog();
            this.rainbow = pEvent.isRainbow();
        }

        public static AddOperation decode(FriendlyByteBuf pBuffer){
            return new AddOperation(pBuffer);
        }

        public OperationType getType(){
            return OperationType.ADD;
        }

        public void dispatch(UUID pId, Handler pHandler){
            pHandler.add(pId, this.clientBossbarType, this.texture, this.name, this.health, this.maxHealth, new Col(r, g, b), this.bossMusic, this.darkenScreen, this.playBossMusic, this.createWorldFog, this.rainbow);
        }

        public void encode(FriendlyByteBuf pBuffer){
            pBuffer.writeResourceLocation(this.clientBossbarType);
            pBuffer.writeResourceLocation(this.texture);
            pBuffer.writeComponent(this.name);
            pBuffer.writeFloat(this.health);
            pBuffer.writeFloat(this.maxHealth);
            pBuffer.writeFloat(this.r);
            pBuffer.writeFloat(this.g);
            pBuffer.writeFloat(this.b);
            pBuffer.writeResourceLocation(this.bossMusic.getLocation());
            pBuffer.writeBoolean(this.darkenScreen);
            pBuffer.writeBoolean(this.playBossMusic);
            pBuffer.writeBoolean(this.createWorldFog);
            pBuffer.writeBoolean(this.rainbow);
        }
    }

    record UpdateNameOperation(Component name) implements Operation{
        private UpdateNameOperation(FriendlyByteBuf pBuffer){
            this(pBuffer.readComponent());
        }

        public OperationType getType(){
            return OperationType.UPDATE_NAME;
        }

        public void dispatch(UUID pId, Handler pHandler){
            pHandler.updateName(pId, this.name);
        }

        public void encode(FriendlyByteBuf pBuffer){
            pBuffer.writeComponent(this.name);
        }
    }

    static class UpdateProgressOperation implements Operation{
        public float health;
        public float maxHealth;

        UpdateProgressOperation(float health, float maxHealth){
            this.health = health;
            this.maxHealth = maxHealth;
        }

        private UpdateProgressOperation(FriendlyByteBuf pBuffer){
            this.health = pBuffer.readFloat();
            this.maxHealth = pBuffer.readFloat();
        }

        public OperationType getType(){
            return OperationType.UPDATE_PROGRESS;
        }

        public void dispatch(UUID pId, Handler pHandler){
            pHandler.updateProgress(pId, this.health, this.maxHealth);
        }

        public void encode(FriendlyByteBuf pBuffer){
            pBuffer.writeFloat(this.health);
            pBuffer.writeFloat(this.maxHealth);
        }
    }

    record UpdatePropertiesOperation(ResourceLocation type, ResourceLocation texture, SoundEvent event, boolean darkenScreen, boolean playMusic, boolean createWorldFog, boolean isRainbow, boolean aboutToDie) implements Operation{
        private UpdatePropertiesOperation(FriendlyByteBuf pBuffer){
            this(
            pBuffer.readResourceLocation(),
            pBuffer.readResourceLocation(),
            SoundEvent.createFixedRangeEvent(pBuffer.readResourceLocation(), 16),
            pBuffer.readBoolean(),
            pBuffer.readBoolean(),
            pBuffer.readBoolean(),
            pBuffer.readBoolean(),
            pBuffer.readBoolean());
        }

        public OperationType getType(){
            return OperationType.UPDATE_PROPERTIES;
        }

        public void dispatch(UUID pId, Handler pHandler){
            pHandler.updateProperties(pId, this.type, this.texture, this.event, this.darkenScreen, this.playMusic, this.createWorldFog, this.isRainbow, this.aboutToDie);
        }

        public void encode(FriendlyByteBuf pBuffer){
            pBuffer.writeResourceLocation(this.type);
            pBuffer.writeResourceLocation(this.texture);
            pBuffer.writeResourceLocation(this.event.getLocation());
            pBuffer.writeBoolean(this.darkenScreen);
            pBuffer.writeBoolean(this.playMusic);
            pBuffer.writeBoolean(this.createWorldFog);
            pBuffer.writeBoolean(this.isRainbow);
            pBuffer.writeBoolean(this.aboutToDie);
        }
    }

    static class UpdateStyleOperation implements Operation{
        public float r, g, b;

        UpdateStyleOperation(Col col){
            this.r = col.r;
            this.g = col.g;
            this.b = col.b;
        }

        private UpdateStyleOperation(FriendlyByteBuf pBuffer){
            r = pBuffer.readFloat();
            g = pBuffer.readFloat();
            b = pBuffer.readFloat();
        }

        public OperationType getType(){
            return OperationType.UPDATE_STYLE;
        }

        public void dispatch(UUID pId, Handler pHandler){
            pHandler.updateStyle(pId, new Col(r, g, b));
        }

        public void encode(FriendlyByteBuf pBuffer){
            pBuffer.writeFloat(this.r);
            pBuffer.writeFloat(this.g);
            pBuffer.writeFloat(this.b);
        }
    }
}