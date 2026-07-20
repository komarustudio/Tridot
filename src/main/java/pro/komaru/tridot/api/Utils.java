package pro.komaru.tridot.api;

import com.google.common.collect.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import net.minecraft.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.loading.*;
import net.minecraftforge.items.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.*;
import pro.komaru.tridot.client.render.TridotRenderTypes;
import pro.komaru.tridot.client.render.RenderBuilder;
import pro.komaru.tridot.client.render.DotRenderType;
import pro.komaru.tridot.client.model.render.item.CustomItemRenderer;
import pro.komaru.tridot.common.*;
import pro.komaru.tridot.common.compatibility.snakeyaml.internal.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.struct.Structs;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.func.Boolf;
import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.math.ArcRandom;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;
import pro.komaru.tridot.common.registry.item.skins.ItemSkin;

import javax.annotation.*;
import java.lang.Math;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static net.minecraft.util.Mth.sqrt;

public class Utils {
    public static ArcRandom rand = Tmp.rnd;

    /**
     * Checks if the game was started in IDE
     */
    public static boolean isDevelopment = !FMLLoader.isProduction();

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static LocalPlayer player() {
        return mc().player;
    }

    public static Seq<ServerPlayer> players() {
        return Seq.with(server().getPlayerList().getPlayers());
    }

    public static void player(Cons<ServerPlayer> cons) {
        players().each(cons);
    }

    public static Object parse(Tag obj) {
        if(obj instanceof ByteTag b) return b.getAsByte();
        if(obj instanceof ShortTag s) return s.getAsShort();
        if(obj instanceof IntTag i) return i.getAsInt();
        if(obj instanceof LongTag l) return l.getAsLong();
        if(obj instanceof FloatTag f) return f.getAsFloat();
        if(obj instanceof DoubleTag d) return d.getAsDouble();
        if(obj instanceof ByteArrayTag b) return b.getAsByteArray();
        if(obj instanceof StringTag s) return s.getAsString();
        if(obj instanceof ListTag l) {
            List<Object> list = new ArrayList<>();
            for (Tag tag : l)
                list.add(parse(tag));
            return list;
        }
        if(obj instanceof CompoundTag nbt) {
            Map<String,Object> map = new LinkedHashMap<>();
            for (String k : nbt.getAllKeys())
                map.put(k,parse(nbt.get(k)));
            return map;
        }
        try {
            return TagParser.parseTag(obj.toString());
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public static Tag parse(Object obj) {
        if(obj instanceof Byte b) return ByteTag.valueOf(b);
        else if(obj instanceof Short s) return ShortTag.valueOf(s);
        else if(obj instanceof Integer i) return IntTag.valueOf(i);
        else if(obj instanceof Long l) return LongTag.valueOf(l);
        else if(obj instanceof Float f) return FloatTag.valueOf(f);
        else if(obj instanceof Double d) return DoubleTag.valueOf(d);
        else if(obj instanceof byte[] bs) return new ByteArrayTag(bs);
        else if(obj instanceof String str) return StringTag.valueOf(str);
        else if(obj instanceof List<?> l) {
            ListTag tag = new ListTag();
            for (Object o : l)
                tag.add(parse(o));
            return tag;
        }
        else if (obj instanceof Object[] os){
            ListTag tag = new ListTag();
            for (Object o : os)
                tag.add(parse(o));
            return tag;
        }
        else if (obj instanceof Map<?,?> map) {
            CompoundTag tag = new CompoundTag();
            map.forEach((k,v) -> tag.put(String.valueOf(k),parse(v)));
            return tag;
        }
        return EndTag.INSTANCE;
    }

    public static void put(CompoundTag nbt, String key, Object obj) {
        nbt.put(key,parse(obj));
    }

    /**Particles utils*/
    public static class Particles {

        /**
         * Spawns particles in radius like in radiusHit
         *
         * @param radius Distance in blocks
         * @param stack  Stack to add radius enchantment levels (can be null)
         * @param type   Particle that will spawn at radius
         * @param pos    Position
         */
        public static void inRadius(Level level, @Nullable ItemStack stack, ParticleOptions type, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            float pRadius = stack != null ? radius + Items.enchantmentRadius(stack) : radius;
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((i + yawRaw + 90) * Math.PI) / 180;
                double X = Math.sin(pitch) * Math.cos(yaw) * pRadius * 0.75F;
                double Y = Math.cos(pitch) * pRadius * 0.75F;
                double Z = Math.sin(pitch) * Math.sin(yaw) * pRadius * 0.75F;
                if (!level.isClientSide() && level instanceof ServerLevel pServer) {
                    pServer.sendParticles(type, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z, 1, 0, 0, 0, 0);
                }
            }
        }

        /**
         * @param hitEntities List for damaged entities
         * @param type        Particle that will appear at marked mobs
         * @param pos         Position
         * @param radius      Distance in blocks
         */
        public static void mark(Level level, Player player, List<LivingEntity> hitEntities, ParticleOptions type, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((i + yawRaw + 90) * Math.PI) / 180;

                double X = Math.sin(pitch) * Math.cos(yaw) * radius;
                double Y = Math.cos(pitch) * radius;
                double Z = Math.sin(pitch) * Math.sin(yaw) * radius;

                AABB boundingBox = new AABB(pos.x, pos.y - 8 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(player)) {
                        hitEntities.add(livingEntity);
                        if (!livingEntity.isAlive()) {
                            return;
                        }

                        if (!level.isClientSide() && level instanceof ServerLevel pServer) {
                            pServer.sendParticles(type, livingEntity.getX(), livingEntity.getY() + 2 + ((rand.nextFloat() - 0.5D) * 0.2F), livingEntity.getZ(), 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }

        /**
         * Spawns particles around position
         *
         * @param distance Distance in blocks
         * @param options  Particle that will spawn at radius
         * @param speed    Speed of particles
         * @param pos      Position
         */
        public static void around(Vector3d pos, float distance, float speed, Level level, ParticleOptions options) {
            RandomSource source = RandomSource.create();
            for (int i = 0; i < 360; i += 10) {
                double X = ((rand.nextDouble() - 0.5D) * distance);
                double Y = ((rand.nextDouble() - 0.5D) * distance);
                double Z = ((rand.nextDouble() - 0.5D) * distance);

                double dX = -X;
                double dY = -Y;
                double dZ = -Z;
                if (!level.isClientSide() && level instanceof ServerLevel pServer) {
                    for (int ii = 0; ii < 1 + Mth.nextInt(source, 0, 2); ii += 1) {
                        double yaw = Math.atan2(dZ, dX) + i;
                        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
                        double XX = Math.sin(pitch) * Math.cos(yaw) * speed / (ii + 1);
                        double YY = Math.sin(pitch) * Math.sin(yaw) * speed / (ii + 1);
                        double ZZ = Math.cos(pitch) * speed / (ii + 1);

                        pServer.sendParticles(options, pos.x + X, pos.y + Y, pos.z + Z, 1, XX, YY, ZZ, 0);
                    }
                }
            }
        }

        /**
         * Spawns particles line to attacked mob position
         *
         * @param pPlayer   Player pos for calculating Attacked mob and positions
         * @param pType     Particle that will spawn line
         * @param pDuration cooldown
         */
        public static void lineToAttacked(Level pLevel, Player pPlayer, ParticleOptions pType, int pDuration){
            LivingEntity lastHurtMob = pPlayer.getLastAttacker();
            if(!pLevel.isClientSide() && pLevel instanceof ServerLevel pServer){
                if(lastHurtMob == null){
                    return;
                }

                Vec3 pos = new Vec3(pPlayer.getX(), pPlayer.getY() + 0.5f, pPlayer.getZ());
                Vec3 EndPos = new Vec3(lastHurtMob.getX(), lastHurtMob.getY() + 0.5f, lastHurtMob.getZ());
                double distance = pos.distanceTo(EndPos);
                double distanceInBlocks = Math.floor(distance);
                for(pDuration = 0; pDuration < distanceInBlocks; pDuration++){
                    double dX = pos.x - EndPos.x;
                    double dY = pos.y - EndPos.y;
                    double dZ = pos.z - EndPos.z;
                    float x = (float)(dX / distanceInBlocks);
                    float y = (float)(dY / distanceInBlocks);
                    float z = (float)(dZ / distanceInBlocks);

                    pServer.sendParticles(pType, pos.x - (x * pDuration), pos.y - (y * pDuration), pos.z - (z * pDuration), 1, 0, 0, 0, 0);
                }
            }
        }

        /**
         * Spawns particles line to attacked mob position
         *
         * @param pPlayer Player pos for calculating Attacked mob and positions
         * @param pType   Particle that will spawn line
         */
        public static void lineToAttacked(Level pLevel, Player pPlayer, ParticleOptions pType) {
            LivingEntity lastHurtMob = pPlayer.getLastAttacker();
            if (!pLevel.isClientSide() && pLevel instanceof ServerLevel pServer) {
                if (lastHurtMob == null) {
                    return;
                }

                Vec3 pos = new Vec3(pPlayer.getX(), pPlayer.getY() + 0.5f, pPlayer.getZ());
                Vec3 EndPos = new Vec3(lastHurtMob.getX(), lastHurtMob.getY() + 0.5f, lastHurtMob.getZ());
                double distance = pos.distanceTo(EndPos);
                double distanceInBlocks = Math.floor(distance);
                for (int i = 0; i < distanceInBlocks; i++) {
                    double dX = pos.x - EndPos.x;
                    double dY = pos.y - EndPos.y;
                    double dZ = pos.z - EndPos.z;
                    float x = (float) (dX / distanceInBlocks);
                    float y = (float) (dY / distanceInBlocks);
                    float z = (float) (dZ / distanceInBlocks);

                    pServer.sendParticles(pType, pos.x - (x * i), pos.y - (y * i), pos.z - (z * i), 1, 0, 0, 0, 0);
                }
            }
        }

        /**
         * Spawns particle lines to nearby Mobs
         *
         * @param pPlayer     Player for reciving pos from
         * @param pType       Particle type to spawn
         * @param hitEntities list of Entities
         * @param pos         Position in Vec3
         * @param radius      Radius to spawn
         */
        public static void lineToNearby(Level pLevel, Player pPlayer, ParticleOptions pType, List<LivingEntity> hitEntities, Vec3 pos, float pitchRaw, float yawRaw, float radius) {
            double pitch = ((pitchRaw + 90) * Math.PI) / 180;
            double yaw = ((yawRaw + 90) * Math.PI) / 180;

            double X = Math.sin(pitch) * Math.cos(yaw) * radius;
            double Y = Math.cos(pitch) * radius;
            double Z = Math.sin(pitch) * Math.sin(yaw) * radius;
            AABB boundingBox = new AABB(pos.x, pos.y - 8 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
            List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, boundingBox);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(pPlayer)) {
                    hitEntities.add(livingEntity);
                    if (!livingEntity.isAlive()) {
                        return;
                    }

                    Vec3 pTo = new Vec3(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    double distance = pos.distanceTo(pTo);
                    double distanceInBlocks = Math.floor(distance);
                    for (int i = 0; i < distanceInBlocks; i++) {
                        double dX = pos.x - pTo.x;
                        double dY = pos.y - pTo.y;
                        double dZ = pos.z - pTo.z;

                        float x = (float) (dX / distanceInBlocks);
                        float y = (float) (dY / distanceInBlocks);
                        float z = (float) (dZ / distanceInBlocks);

                        if (!pLevel.isClientSide() && pLevel instanceof ServerLevel pServer) {
                            pServer.sendParticles(pType, pos.x - (x * i), pos.y - (y * i), pos.z - (z * i), 1, 0, 0, 0, 0);
                        }
                    }

                    for (int i = 0; i < 3; i++) {
                        pLevel.addParticle(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 0, 0, 0);
                    }
                }
            }
        }

        /**
         * Spawns particles line
         *
         * @param pType Particle that will spawn line
         * @param pFrom Position From
         * @param pTo   Position To
         */
        public static void line(Level pLevel, Vec3 pFrom, Vec3 pTo, ParticleOptions pType) {
            double distance = pFrom.distanceTo(pTo);
            double distanceInBlocks = Math.floor(distance);
            for (int i = 0; i < distanceInBlocks; i++) {
                double dX = pFrom.x - pTo.x;
                double dY = pFrom.y - pTo.y;
                double dZ = pFrom.z - pTo.z;
                float x = (float) (dX / distanceInBlocks);
                float y = (float) (dY / distanceInBlocks);
                float z = (float) (dZ / distanceInBlocks);

                if (!pLevel.isClientSide() && pLevel instanceof ServerLevel pServer) {
                    pServer.sendParticles(pType, pFrom.x - (x * i), pFrom.y - (y * i), pFrom.z - (z * i), 1, 0, 0, 0, 0);
                }
            }
        }
    }
    /**Blocks utils*/
    public static class Blocks {

        /**
         * @param pSize   Portal Size
         * @param pPortal Portal State
         * @param pFrame  Portal Frame
         */
        public static void endShapedPortal(int pSize, Level pDestination, BlockPos pPos, BlockState pPortal, BlockState pFrame) {
            for (int i = 0; i < pSize - 1; ++i) {
                for (int j = 0; j < pSize - 1; ++j) {
                    pDestination.setBlock(pPos.offset(i, 0, j), pPortal, 2);
                }
            }

            for (int i = 0; i < pSize; i++) {
                for (int j = 0; j < pSize; j++) {
                    if (i == 0 || i == pSize - 1 || j == 0 || j == pSize - 1) {
                        pDestination.setBlock(pPos.offset(i, 0, j), pFrame, 2);
                    }
                }
            }
        }

        public static boolean growCrop(ItemStack stack, Level level, BlockPos blockPos){
            if(BoneMealItem.growCrop(stack, level, blockPos)){
                return true;
            }else{
                BlockState blockstate = level.getBlockState(blockPos);
                boolean flag = blockstate.isFaceSturdy(level, blockPos, Direction.UP);
                return flag && BoneMealItem.growWaterPlant(stack, level, blockPos.relative(Direction.UP), Direction.UP);
            }
        }

        public static boolean growCrop(Level level, BlockPos blockPos){
            return growCrop(ItemStack.EMPTY, level, blockPos);
        }

        public static ToIntFunction<BlockState> light(int pValue) {
            return (state) -> !state.isAir() ? pValue : 0;
        }

        public static ToIntFunction<BlockState> lightIfLit(int pValue) {
            return (state) -> state.getValue(BlockStateProperties.LIT) ? pValue : 0;
        }

        public static ToIntFunction<BlockState> lightIfLit() {
            return (state) -> state.getValue(BlockStateProperties.LIT) ? 14 : 0;
        }

        public static ToIntFunction<BlockState> plantLight() {
            return (state) -> !state.isAir() ? 12 : 0;
        }
    }
    /**Attacking, hitting utils*/
    public static class Hit {

        /**
         * Calculates power though a seen percent, used in TNT's, and Necromancer Boss to define power of knockback
         */
        public static float seenPercent(Vec3 pVector, Entity pEntity, float pStrength){
            AABB aabb = pEntity.getBoundingBox();
            double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
            double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
            double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
            double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
            double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
            if(!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)){
                int i = 0;
                int j = 0;
                for(double d5 = 0.0D; d5 <= 1.0D; d5 += d0){
                    for(double d6 = 0.0D; d6 <= 1.0D; d6 += d1){
                        for(double d7 = 0.0D; d7 <= 1.0D; d7 += d2){
                            double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                            double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                            double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                            Vec3 vec3 = new Vec3(d8 + d3, d9, d10 + d4);
                            if(pEntity.level().clip(new ClipContext(vec3, pVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pEntity)).getType() == HitResult.Type.MISS){
                                ++i;
                            }

                            ++j;
                        }
                    }
                }

                return ((float)i / (float)j) * pStrength;
            }else{
                return pStrength;
            }
        }

        /**
         * Performs a spin attack with checking a collision with targets
         */
        public static void circularHit(Level level, Player player) {
            List<Entity> list = level.getEntities(player, player.getBoundingBox().inflate(1));
            float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE)) + EnchantmentHelper.getSweepingDamageRatio(player);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity instanceof LivingEntity target) {
                        target.hurt(level.damageSources().playerAttack(player), (damage + EnchantmentHelper.getDamageBonus(player.getUseItem(), target.getMobType())) * 1.35f);
                    }
                }
            }
        }

        public static void circularHit(Level level, Player player, double inflateValue) {
            List<Entity> list = level.getEntities(player, player.getBoundingBox().inflate(inflateValue));
            float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE)) + EnchantmentHelper.getSweepingDamageRatio(player);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity instanceof LivingEntity target) {
                        target.hurt(level.damageSources().playerAttack(player), (damage + EnchantmentHelper.getDamageBonus(player.getUseItem(), target.getMobType())) * 1.35f);
                    }
                }
            }
        }

        /**
         * Performs a circled attack near player
         *
         * @param radius      Attack radius
         * @param type        Particle type used to show radius
         * @param hitEntities List for damaged entities
         * @param pos         Position
         */
        public static void circularHit(Level level, ItemStack stack, Player player, @Nullable ParticleOptions type, List<LivingEntity> hitEntities, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((yawRaw + 90) * Math.PI) / 180;
                float pRadius = radius + Items.enchantmentRadius(stack);
                double X = Math.sin(pitch) * Math.cos(yaw + i) * pRadius;
                double Y = Math.cos(pitch) * pRadius;
                double Z = Math.sin(pitch) * Math.sin(yaw + i) * pRadius;

                AABB boundingBox = new AABB(pos.x, pos.y - 1 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(player)) {
                        if(Utils.Entities.canHitTarget(player, livingEntity)) hitEntities.add(livingEntity);
                    }
                }

                X = Math.sin(pitch) * Math.cos(yaw + i) * pRadius * 0.75F;
                Y = Math.cos(pitch) * pRadius * 0.75F;
                Z = Math.sin(pitch) * Math.sin(yaw + i) * pRadius * 0.75F;
                if (type != null && !level.isClientSide() && level instanceof ServerLevel pServer) {
                    pServer.sendParticles(type, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z, 1, 0, 0, 0, 0);
                }
            }
        }

        /**
         * Performs a circled attack near player
         *
         * @param radius      Attack radius
         * @param type        Particle type used to show radius
         * @param hitEntities List for damaged entities
         * @param pos         Position
         */
        public static void circularHit(Level level, Player player, @Nullable ParticleOptions type, List<LivingEntity> hitEntities, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((yawRaw + 90) * Math.PI) / 180;
                double X = Math.sin(pitch) * Math.cos(yaw + i) * radius;
                double Y = Math.cos(pitch) * radius;
                double Z = Math.sin(pitch) * Math.sin(yaw + i) * radius;

                AABB boundingBox = new AABB(pos.x, pos.y - 1 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, boundingBox);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(player)) {
                        if(Utils.Entities.canHitTarget(player, livingEntity)) hitEntities.add(livingEntity);
                    }
                }

                X = Math.sin(pitch) * Math.cos(yaw + i) * radius * 0.75F;
                Y = Math.cos(pitch) * radius * 0.75F;
                Z = Math.sin(pitch) * Math.sin(yaw + i) * radius * 0.75F;
                if (type != null && !level.isClientSide() && level instanceof ServerLevel pServer) {
                    pServer.sendParticles(type, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z, 1, 0, 0, 0, 0);
                }
            }
        }

        public static void hitLast(Level pLevel, Player pPlayer, float pAmount) {
            LivingEntity lastHurtMob = pPlayer.getLastAttacker();
            if (!pLevel.isClientSide() && pLevel instanceof ServerLevel pServer) {
                if (lastHurtMob == null) {
                    return;
                }

                lastHurtMob.hurt(pServer.damageSources().playerAttack(pPlayer), pAmount);
            }
        }

        /**
         * @param pPlayer     Player for reciving pos from
         * @param hitEntities list of Entities
         * @param pos         Position in Vec3
         * @param radius      Radius to check mobs
         */
        public static void markNearbyMobs(Level pLevel, Player pPlayer, List<LivingEntity> hitEntities, Vec3 pos, float pitchRaw, float yawRaw, float radius) {
            double pitch = ((pitchRaw + 90) * Math.PI) / 180;
            double yaw = ((yawRaw + 90) * Math.PI) / 180;

            double X = Math.sin(pitch) * Math.cos(yaw) * radius;
            double Y = Math.cos(pitch) * radius;
            double Z = Math.sin(pitch) * Math.sin(yaw) * radius;
            AABB boundingBox = new AABB(pos.x, pos.y - 8 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
            List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, boundingBox);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(pPlayer)) {
                    hitEntities.add(livingEntity);
                    if (!livingEntity.isAlive()) {
                        return;
                    }

                    livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                }
            }
        }

        /**
         * @param pType       EntityType to heal
         * @param pHealer     Healer entity
         * @param hitEntities list of Entities
         * @param pos         Position in Vec3
         * @param radius      Radius to check mobs
         */
        public static void healNearbyMobs(MobCategory pType, Float pHeal, Level pLevel, LivingEntity pHealer, List<LivingEntity> hitEntities, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((yawRaw + 90) * Math.PI) / 180;
                double X = Math.sin(pitch) * Math.cos(yaw + i) * radius;
                double Y = Math.cos(pitch) * radius;
                double Z = Math.sin(pitch) * Math.sin(yaw + i) * radius;

                AABB boundingBox = new AABB(pos.x, pos.y - 8 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
                List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, boundingBox);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(pHealer) && pType.equals(entity.getType().getCategory())) {
                        hitEntities.add(livingEntity);
                        if (!livingEntity.isAlive()) {
                            return;
                        }

                        livingEntity.heal(pHeal);
                    }
                }
            }
        }

        /**
         * @param pHealer     Healer entity
         * @param hitEntities list of Entities
         * @param pos         Position in Vec3
         * @param radius      Radius to check mobs
         */
        public static void healNearbyMobs(float pHeal, Level pLevel, LivingEntity pHealer, List<LivingEntity> hitEntities, Vector3d pos, float pitchRaw, float yawRaw, float radius) {
            for (int i = 0; i < 360; i += 10) {
                double pitch = ((pitchRaw + 90) * Math.PI) / 180;
                double yaw = ((yawRaw + 90) * Math.PI) / 180;
                double X = Math.sin(pitch) * Math.cos(yaw + i) * radius;
                double Y = Math.cos(pitch) * radius;
                double Z = Math.sin(pitch) * Math.sin(yaw + i) * radius;

                AABB boundingBox = new AABB(pos.x, pos.y - 8 + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z, pos.x + X, pos.y + Y + ((rand.nextFloat() - 0.5D) * 0.2F), pos.z + Z);
                List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, boundingBox);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity && !hitEntities.contains(livingEntity) && !livingEntity.equals(pHealer)) {
                        hitEntities.add(livingEntity);
                        if (!livingEntity.isAlive()) {
                            return;
                        }

                        livingEntity.heal(pHeal);
                    }
                }
            }
        }

        /**
         * @param from   pos from
         * @param entity entity (projectile, player etc.
         * @param filter (e) -> true as default
         * @param to     pos to
         * @param level  level
         * @return HitResult
         */
        public static HitResult hitResult(Vec3 from, Entity entity, Predicate<Entity> filter, Vec3 to, Level level) {
            Vec3 vec3 = from.add(to);
            HitResult hitresult = level.clip(new ClipContext(from, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec3 = hitresult.getLocation();
            }

            HitResult result = ProjectileUtil.getEntityHitResult(level, entity, from, vec3, entity.getBoundingBox().expandTowards(to).inflate(1.0D), filter);
            if (result != null) {
                hitresult = result;
            }

            return hitresult;
        }

        /**
         * Imitates tnt explosion from player with defined radius, damage and knockback
         * @param player Player that casts the explosion
         * @param itemstack Item that casts the explosion
         * @param pos Position of the explosion
         * @param clipPos
         * @param radius Radius of the explosion
         * @param damage Damage the explosion does
         * @param knockback Knockback the explosion does
         */
        public static void explosion(Player player, ItemStack itemstack, Vec3 pos, Vec3 clipPos, float radius, float damage, float knockback) {
            Level level = player.level();
            RandomSource rand = level.random;
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(pos.x + clipPos.x - radius, pos.y + clipPos.y - radius, pos.z + clipPos.z - radius, pos.x + clipPos.x + radius, pos.y + clipPos.y + radius, pos.z + clipPos.z + radius));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity enemy) {
                    if (!enemy.equals(player)) {
                        enemy.hurt(level.damageSources().explosion(player, player), damage);
                        enemy.knockback(knockback, player.getX() + clipPos.x - entity.getX(), player.getZ() + clipPos.z - entity.getZ());
                        if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FIRE_ASPECT, itemstack) > 0) {
                            int i = EnchantmentHelper.getFireAspect(player);
                            enemy.setSecondsOnFire(i * 4);
                        }
                    }
                }
            }

            if (level instanceof ServerLevel srv) {
                srv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x + clipPos.x, pos.y + clipPos.y, player.getZ() + clipPos.z, 1, 0, 0, 0, radius);
                srv.playSound(null, player.blockPosition().offset((int) clipPos.x, (int) (clipPos.y + player.getEyeHeight()), (int) clipPos.z), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 10f, 1f);
                srv.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x + clipPos.x + ((rand.nextDouble() - 0.5D) * radius), pos.y + clipPos.y + ((rand.nextDouble() - 0.5D) * radius), pos.z + clipPos.z + ((rand.nextDouble() - 0.5D) * radius), 8, 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.2f);
                srv.sendParticles(ParticleTypes.FLAME, pos.x + clipPos.x + ((rand.nextDouble() - 0.5D) * radius), pos.y + clipPos.y + ((rand.nextDouble() - 0.5D) * radius), pos.z + clipPos.z + ((rand.nextDouble() - 0.5D) * radius), 6, 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.05d * ((rand.nextDouble() - 0.5D) * radius), 0.2f);
            }
        }
    }
    /**Items utils**/
    public static class Items {
        public static Item getItem(String modId, String id){
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, id));
            return item != null ? item : net.minecraft.world.item.Items.DIRT;
        }

        public static boolean getAttackStrengthScale(Player player, float powerPercent) {
            float f2 = player.getAttackStrengthScale(0.5F);
            return f2 > powerPercent;
        }

        /**
         * Applies a cooldown to item list
         *
         * @param items         ItemList to apply the cooldown
         * @param cooldownTicks Time of cooldown
         */
        public static void cooldownItems(Player player, List<Item> items, int cooldownTicks) {
            for (Item pItems : items) {
                player.getCooldowns().addCooldown(pItems, cooldownTicks);
            }
        }

        /**
         * @param stack being checked
         * @return 0.5 per level if true
         * @see  Hit#circularHit
         */
        public static float enchantmentRadius(ItemStack stack) {
            int i = stack.getEnchantmentLevel(EnchantmentsRegistry.RADIUS.get());
            return i > 0 ? (float) i / 2 : 0.0F;
        }

        public static void addSkinTooltip(ItemStack stack, List<Component> tooltip){
            ItemSkin skin = ItemSkin.itemSkin(stack);
            if (skin != null) {
                if(skin.getHoverName() != null) {
                    tooltip.remove(0);
                    tooltip.add(0, Component.literal(stack.getHoverName().getString()).append(skin.getHoverName()));
                }

                tooltip.add(1, skin.skinComponent());
                tooltip.add(2, Component.empty());
                if(skin.getComponents() != null) tooltip.addAll(skin.getComponents()); // meant to be added into third index, but I think this way is safer
            }
        }

        /**
         * Searches for itemstack that matches the predicate and
         * @param player player to search the inventory for
         * @param shootable shootable item (e.g. Bow)
         * @param predicate predicate for needed ammo
         * @return projectile itemstack
         */
        public static ItemStack getProjectile(Player player, ItemStack shootable, Boolf<ItemStack> predicate) {
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack ammo = player.getInventory().getItem(i);
                if (predicate.get(ammo)) {
                    return net.minecraftforge.common.ForgeHooks.getProjectile(player, shootable, ammo);
                }
            }

            // why cobblestone? i dunno too
            return player.isCreative() ? net.minecraft.world.item.Items.COBBLESTONE.getDefaultInstance() : ItemStack.EMPTY;
        }

        /**
         * Searches for itemstack that matches the predicate and
         * @param player player to search the inventory for
         * @param shootable shootable item (e.g. Bow)
         * @param tag needed ammo tag
         * @return projectile itemstack
         */
        public static ItemStack getProjectile(Player player, ItemStack shootable, TagKey<Item> tag) {
            Boolf<ItemStack> predicate = (stack) -> stack.is(tag);
                return getProjectile(player, shootable, predicate);
        }

        /**
         * Adds effect tooltips to list
         * @param effects Effect list
         * @param tooltipList Tooltip list
         * @param duration Duration of effects
         * @param chance Chance of effects
         */
        public static void effectTooltip(ImmutableList<MobEffectInstance> effects, List<Component> tooltipList, float duration, float chance) {
            if (!effects.isEmpty()) {
                if (chance > 0 && chance < 1) {
                    tooltipList.add(Component.translatable("tooltip.tridot.applies_with_chance", String.format("%.1f%%", chance * 100)).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipList.add(Component.translatable("tooltip.tridot.applies").withStyle(ChatFormatting.GRAY));
                }

                effectLines(effects, tooltipList, duration);
            }
        }


        /**
         * Adds effect tooltips to list
         * @param effects Effect list
         * @param tooltipList Tooltip list
         * @param duration Duration of effects
         * @param chance Chance of effects
         */
        public static void effectTargetTooltip(ImmutableList<MobEffectInstance> effects, List<Component> tooltipList, float duration, float chance) {
            if (!effects.isEmpty()) {
                if (chance > 0 && chance < 1) {
                    tooltipList.add(Component.translatable("tooltip.tridot.applies_with_chance_target", String.format("%.1f%%", chance * 100)).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipList.add(Component.translatable("tooltip.tridot.applies_to_target").withStyle(ChatFormatting.GRAY));
                }

                effectLines(effects, tooltipList, duration);
            }
        }

        public static void effectLines(ImmutableList<MobEffectInstance> effects, List<Component> tooltipList, float duration){
            for (MobEffectInstance mobeffectinstance : effects) {
                MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
                MobEffect mobeffect = mobeffectinstance.getEffect();
                if (mobeffectinstance.getAmplifier() > 0) {
                    mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (!mobeffectinstance.endsWithin(20)) {
                    mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, duration));
                }

                tooltipList.add(Component.literal(" ♦ ").withStyle(mobeffect.getCategory().getTooltipFormatting()).append(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting())));
            }
        }

        public static LootTable getTable(ServerLevel pServer, ResourceLocation pLoot){
            return pServer.getServer().getLootData().getLootTable(pLoot);
        }

        /**
         * Drops loot on using item
         */
        public static void dropLoot(Player pPlayer, Collection<ItemStack> pItemStacks){
            for(ItemStack stack : pItemStacks){
                pPlayer.drop(stack, false);
            }
        }

        public static void giveLoot(ServerPlayer pPlayer, Collection<ItemStack> pItemStacks){
            for(ItemStack stack : pItemStacks){
                ItemHandlerHelper.giveItemToPlayer(pPlayer, stack);
            }
        }

        /**
         * Spawns loot on a block position when using item
         */
        public static void spawnLoot(Level pLevel, BlockPos pPos, Collection<ItemStack> pItemStacks){
            if(!pLevel.isClientSide()){
                for(ItemStack stack : pItemStacks){
                    pLevel.addFreshEntity(new ItemEntity(pLevel, pPos.getX() + 0.5F, pPos.getY() + 0.5F, pPos.getZ() + 0.5F, stack));
                }
            }
        }

        @Nonnull
        public static List<ItemStack> createLoot(ResourceLocation pLoot, LootParams pParams){
            LootTable loot = getTable(pParams.getLevel(), pLoot);
            if(loot == LootTable.EMPTY) return Lists.newArrayList();
            return loot.getRandomItems(pParams);
        }

        public static LootParams getGiftParameters(ServerLevel pLevel, Vec3 pPos, Entity pEntity){
            return getGiftParameters(pLevel, pPos, 0, pEntity);
        }

        public static LootParams getGiftParameters(ServerLevel pLevel, Vec3 pPos, float pLuckValue, Entity pEntity){
            return new LootParams.Builder(pLevel).withParameter(LootContextParams.THIS_ENTITY, pEntity).withParameter(LootContextParams.ORIGIN, pPos).withLuck(pLuckValue).create(LootContextParamSets.GIFT);
        }

        public static void clearItem(Player player, ItemStack stack) {
            Item item = stack.getItem();
            int count = stack.getCount();
            Inventory inv = player.getInventory();
            inv.clearOrCountMatchingItems((i) ->
                    i.getItem().equals(item), count, player.inventoryMenu.getCraftSlots());
        }

        public static void giveItem(Player player, ItemStack stack) {
            ItemStack outputCopy = stack.copy();
            int k = outputCopy.getCount();
            int i = outputCopy.getMaxStackSize();

            while(k > 0) {
                int l = Math.min(i, k);
                k -= l;
                ItemStack itemstack1 = outputCopy.copy();
                itemstack1.setCount(l);
                boolean flag = player.getInventory().add(itemstack1);
                if (flag && itemstack1.isEmpty()) {
                    itemstack1.setCount(1);
                    ItemEntity itementity1 = player.drop(itemstack1, false);
                    if (itementity1 != null) {
                        itementity1.makeFakeItem();
                    }

                    player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    player.containerMenu.broadcastChanges();
                } else {
                    ItemEntity itementity = player.drop(itemstack1, false);
                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setTarget(player.getUUID());
                    }
                }
            }
        }

        public static FluidStack deserializeFluidStack(JsonObject json){
            String fluidName = GsonHelper.getAsString(json, "fluid");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
            if(fluid == null || fluid == Fluids.EMPTY){
                throw new JsonSyntaxException("Unknown fluid " + fluidName);
            }
            int amount = GsonHelper.getAsInt(json, "amount");
            return new FluidStack(fluid, amount);
        }

        public static MobEffectInstance deserializeMobEffect(JsonObject json){
            String effectName = GsonHelper.getAsString(json, "effect");
            MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
            if(mobEffect == null){
                throw new JsonSyntaxException("Unknown effect " + effectName);
            }
            int duration = GsonHelper.getAsInt(json, "duration");
            int amplifier = GsonHelper.getAsInt(json, "amplifier");
            return new MobEffectInstance(mobEffect, duration, amplifier);
        }

        public static MobEffectInstance mobEffectFromNetwork(FriendlyByteBuf buffer){
            if(buffer.readBoolean()){
                MobEffect mobEffect = buffer.readRegistryId();
                int duration = buffer.readInt();
                int amplifier = buffer.readInt();
                return new MobEffectInstance(mobEffect, duration, amplifier);
            }
            return null;
        }

        public static void mobEffectToNetwork(MobEffectInstance effect, FriendlyByteBuf buffer){
            if(effect == null){
                buffer.writeBoolean(false);
            }else{
                buffer.writeBoolean(true);
                buffer.writeRegistryId(ForgeRegistries.MOB_EFFECTS, effect.getEffect());
                buffer.writeInt(effect.getDuration());
                buffer.writeInt(effect.getAmplifier());
            }

        }

        public static Enchantment deserializeEnchantment(JsonObject json){
            String enchantmentName = GsonHelper.getAsString(json, "enchantment");
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentName));
            if(enchantment == null){
                throw new JsonSyntaxException("Unknown enchantment " + enchantmentName);
            }
            return enchantment;
        }

        public static Enchantment enchantmentFromNetwork(FriendlyByteBuf buffer){
            return !buffer.readBoolean() ? null : buffer.readRegistryId();
        }

        public static void enchantmentToNetwork(Enchantment enchantment, FriendlyByteBuf buffer){
            if(enchantment == null){
                buffer.writeBoolean(false);
            }else{
                buffer.writeBoolean(true);
                buffer.writeRegistryId(ForgeRegistries.ENCHANTMENTS, enchantment);
            }
        }
    }
    /**Vector and other mc physics utilities*/
    public static class Physics {
        /**
         * Can be used in projectile tick() method.
         * Projectile will have a homing movement to nearby entity
         *
         * @param pOwner      Owner of Projectile
         * @param boundingBox radius example:
         *                    <p>
         *                    <pre>{@code new AABB(projectile.getX() - 3.5, projectile.getY() - 0.5, projectile.getZ() - 3.5, projectile.getX() + 3.5, projectile.getY() + 0.5, projectile.getZ() + 3.5);
         *                    }</pre>
         */
        public static void homingTo(double strength, Entity projectile, Level level, Entity pOwner, AABB boundingBox) {
            if (level.isClientSide) return;

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, boundingBox);
            LivingEntity nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (LivingEntity target : targets) {
                if (target == pOwner || !target.isAlive()) continue;
                double dist = projectile.distanceToSqr(target);
                if (dist < nearestDist) {
                    nearest = target;
                    nearestDist = dist;
                }
            }

            if (nearest != null) {
                Vec3 currentVelocity = projectile.getDeltaMovement();
                Vec3 toTarget = nearest.position().add(0, 1, 0).subtract(projectile.position()).normalize();
                Vec3 newVelocity = currentVelocity.add(toTarget.scale(strength)).normalize().scale(currentVelocity.length());
                projectile.setDeltaMovement(newVelocity);
                projectile.hurtMarked = true;
            }
        }

        /**
         * @param pos   Position to start from
         * @param entity Entity that being checked to spawn
         * @return Position of safe non-collideable area to spawn
         */
        public static BlockPos nearbySurface(Level pLevel, BlockPos pos, LivingEntity entity) {
            return nearbySurface(pLevel,pos,entity,1);
        }

        /**
         * @param pos   Position to start from
         * @param entity Entity that being checked to spawn
         * @param iterations Number of attempts
         * @return Position of safe non-collideable area to spawn
         */
        public static BlockPos nearbySurface(Level pLevel, BlockPos pos, LivingEntity entity,int iterations) {
            int x = pos.getX() + (rand.nextInt() - rand.nextInt()) * 6;
            int y = pos.getY() + rand.nextInt(1, 2);
            int z = pos.getZ() + (rand.nextInt() - rand.nextInt()) * 6;
            if (pLevel.noCollision(entity, new AABB(x, y, z, x, y, z).inflate(1))) {
                return new BlockPos(x, y, z);
            }
            if(iterations > 1) {
                return nearbySurface(pLevel,pos,entity,iterations-1);
            }
            return null;
        }
    }

    /**Entities utils*/
    public static class Entities {

        /**
         * Checks whether an attacker can hit a target.
         * Internally fires a {@link LivingAttackEvent} to let other mods cancel the attack, such as Cadmus, WG, etc.
         *
         * @param target   the entity being attacked
         * @param attacker the entity performing the attack
         * @return {@code true} if the attack is allowed, {@code false} if canceled
         */
        public static boolean canHitTarget(LivingEntity target, LivingEntity attacker) {
            return canHitTarget(target, attacker, 100F);
        }

        /**
         * Same as {@link #canHitTarget(LivingEntity, LivingEntity)} but with a custom damage amount.
         * <p>
         * Can be useful if we need to check if an attacker can deal a specific amount of damage to a target.
         */
        public static boolean canHitTarget(LivingEntity target, LivingEntity attacker, float amount) {
            DamageSource source = createDamageSource(attacker);
            LivingAttackEvent event = new LivingAttackEvent(target, source, amount);
            return !MinecraftForge.EVENT_BUS.post(event);
        }

        /**
         * Utility needed for {@link #canHitTarget(LivingEntity, LivingEntity)}.
         * <p>
         * Can be used for any other ways tho.
         */
        public static DamageSource createDamageSource(LivingEntity attacker) {
            if (attacker instanceof Player player) return attacker.damageSources().playerAttack(player);

            return attacker.damageSources().mobAttack(attacker);
        }

        public static void applyWithChance(LivingEntity pTarget, ImmutableList<MobEffectInstance> effects, float chance, ArcRandom arcRandom) {
            if (!effects.isEmpty()) {
                if (arcRandom.chance(chance)) {
                    for (MobEffectInstance effectInstance : effects) {
                        pTarget.addEffect(new MobEffectInstance(effectInstance));
                    }
                }
            }
        }
    }

    /**Schedule utils*/
    //todo redo
    public static class Schedule {
        private static ScheduledExecutorService scheduler = null;
        private static final HashMultimap<Integer, Runnable> scheduledSynchTasks = HashMultimap.create();

        public static void syncTask(Runnable run, int ticks) {
            scheduledSynchTasks.put(ServerTickHandler.tick + ticks, run);
        }

        public static void asyncTask(Runnable run, int time, TimeUnit unit) {
            if (scheduler == null) {
                serverStartupTasks();
            }

            scheduler.schedule(run, time, unit);
        }

        public static void serverStartupTasks() {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }

            scheduler = Executors.newScheduledThreadPool(1);
            handleSyncScheduledTasks(null);
        }

        public static void handleSyncScheduledTasks(@Nullable Integer tick) {
            if (scheduledSynchTasks.containsKey(tick)) {
                Iterator<Runnable> tasks = tick == null ? scheduledSynchTasks.values().iterator() : scheduledSynchTasks.get(tick).iterator();
                while (tasks.hasNext()) {
                    try {
                        tasks.next().run();
                    } catch (Exception ex) {
                        Logger.getLogger("Scheduler").warn(ex.getMessage());
                    }

                    tasks.remove();
                }
            }
        }
    }
    /**Text utils*/
    public static class Text {
        public static String cleanText(String text) {
            return text.replaceAll("[^\\p{L}\\p{N}\\s]+", "").toLowerCase();
        }
        public static Component formatf(String template, Object... values) {
            return Component.literal(format(template,values));
        }
        public static String format(String template, Object... values) {
            return format(template, Structs.map(values));
        }
        public static String format(String template, Map<String, Object> values) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String placeholder = "$" + entry.getKey();
                template = template.replace(placeholder, entry.getValue().toString());
            }
            return template;
        }
    }
    /**Render utils*/
    public static class Render{

        public static CustomItemRenderer customItemRenderer;
        public static float blitOffset = 0;

        public static int FULL_BRIGHT = 15728880;

        public static Function<Float, Float> FULL_WIDTH_FUNCTION = (f) -> 1f;
        public static Function<Float, Float> LINEAR_IN_WIDTH_FUNCTION = (f) -> f;
        public static Function<Float, Float> LINEAR_OUT_WIDTH_FUNCTION = (f) -> 1f - f;
        public static Function<Float, Float> LINEAR_IN_ROUND_WIDTH_FUNCTION = (f) -> f == 1 ? 0 : f;
        public static Function<Float, Float> LINEAR_OUT_ROUND_WIDTH_FUNCTION = (f) -> f == 0 ? 0 : 1f - f;
        public static Function<Float, Float> LINEAR_IN_SEMI_ROUND_WIDTH_FUNCTION = (f) -> f == 1 ? 0.5f : f;
        public static Function<Float, Float> LINEAR_OUT_SEMI_ROUND_WIDTH_FUNCTION = (f) -> f == 0 ? 0.5f : 1f - f;

        public static ShaderInstance getShader(RenderType type){
            if(type instanceof DotRenderType renderType){
                Optional<Supplier<ShaderInstance>> shader = renderType.state.shaderState.shader;
                if(shader.isPresent()){
                    return shader.get().get();
                }
            }
            return null;
        }

        public static CustomItemRenderer getCustomItemRenderer(){
            Minecraft minecraft = Minecraft.getInstance();
            if(customItemRenderer == null) customItemRenderer = new CustomItemRenderer(minecraft, minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), minecraft.getItemRenderer().getBlockEntityRenderer());
            return customItemRenderer;
        }

        /**
         * This code belongs to its author, and licensed under GPL-2.0 license
         * @author MaxBogomol
         */
        public static void renderAura(RenderBuilder builder, PoseStack poseStack, float radius, float size, int longs, boolean floor){
            Matrix4f last = poseStack.last().pose();
            RenderBuilder.VertexConsumerActor supplier = builder.getSupplier();
            VertexConsumer vertexConsumer = builder.getVertexConsumer();

            float startU = 0;
            float endU = Mth.PI * 2;
            float stepU = (endU - startU) / longs;
            for(int i = 0; i < longs; ++i){
                float u = i * stepU + startU;
                float un = (i + 1 == longs) ? endU : (i + 1) * stepU + startU;
                Vector3f p0 = new Vector3f((float)Math.cos(u) * radius, 0, (float)Math.sin(u) * radius);
                Vector3f p1 = new Vector3f((float)Math.cos(un) * radius, 0, (float)Math.sin(un) * radius);

                float textureU = builder.u0;
                float textureV = builder.v0;
                float textureUN = builder.u1;
                float textureVN = builder.v1;
                if(builder.firstSide){
                    supplier.placeVertex(vertexConsumer, last, builder, p0.x(), size, p0.z(), builder.r2, builder.g2, builder.b2, builder.a2, textureU, textureVN, builder.l2);
                    supplier.placeVertex(vertexConsumer, last, builder, p1.x(), size, p1.z(), builder.r2, builder.g2, builder.b2, builder.a2, textureUN, textureVN, builder.l2);
                    supplier.placeVertex(vertexConsumer, last, builder, p1.x(), 0, p1.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureUN, textureV, builder.l1);
                    supplier.placeVertex(vertexConsumer, last, builder, p0.x(), 0, p0.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureU, textureV, builder.l1);
                }

                if(builder.secondSide){
                    supplier.placeVertex(vertexConsumer, last, builder, p0.x(), 0, p0.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureUN, textureV, builder.l1);
                    supplier.placeVertex(vertexConsumer, last, builder, p1.x(), 0, p1.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureU, textureV, builder.l1);
                    supplier.placeVertex(vertexConsumer, last, builder, p1.x(), size, p1.z(), builder.r2, builder.g2, builder.b2, builder.a2, textureU, textureVN, builder.l2);
                    supplier.placeVertex(vertexConsumer, last, builder, p0.x(), size, p0.z(), builder.r2, builder.g2, builder.b2, builder.a2, textureUN, textureVN, builder.l2);
                }

                if(floor){
                    if(builder.firstSide){
                        supplier.placeVertex(vertexConsumer, last, builder, 0, 0.1f, 0, builder.r2, builder.g2, builder.b2, builder.a2, textureU, textureVN, builder.l2);
                        supplier.placeVertex(vertexConsumer, last, builder, 0, 0.1f, 0, builder.r2, builder.g2, builder.b2, builder.a2, textureUN, textureVN, builder.l2);
                        supplier.placeVertex(vertexConsumer, last, builder, p1.x(), 0, p1.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureUN, textureV, builder.l1);
                        supplier.placeVertex(vertexConsumer, last, builder, p0.x(), 0, p0.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureU, textureV, builder.l1);
                    }

                    if(builder.secondSide){
                        supplier.placeVertex(vertexConsumer, last, builder, p0.x(), 0, p0.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureUN, textureV, builder.l1);
                        supplier.placeVertex(vertexConsumer, last, builder, p1.x(), 0, p1.z(), builder.r1, builder.g1, builder.b1, builder.a1, textureU, textureV, builder.l1);
                        supplier.placeVertex(vertexConsumer, last, builder, 0, 0, 0, builder.r2, builder.g2, builder.b2, builder.a2, textureU, textureVN, builder.l2);
                        supplier.placeVertex(vertexConsumer, last, builder, 0, 0, 0, builder.r2, builder.g2, builder.b2, builder.a2, textureUN, textureVN, builder.l2);
                    }
                }
            }
        }

        public static void renderItemModelInGui(ItemStack stack, float x, float y, float xSize, float ySize, float zSize){
            renderItemModelInGui(stack, x, y, xSize, ySize, zSize, 0, 0, 0);
        }

        public static void renderItemModelInGui(ItemStack stack, float x, float y, float xSize, float ySize, float zSize, float xRot, float yRot, float zRot){
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
            CustomItemRenderer customItemRenderer = getCustomItemRenderer();

            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(x, y, 100.0F + blitOffset);
            poseStack.translate((double)xSize / 2, (double)ySize / 2, 0.0D);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            poseStack.scale(xSize, ySize, zSize);
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
            RenderSystem.applyModelViewMatrix();
            PoseStack posestack1 = new PoseStack();
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            boolean flag = !bakedmodel.usesBlockLight();
            if(flag) Lighting.setupForFlatItems();

            customItemRenderer.render(stack, ItemDisplayContext.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);

            RenderSystem.disableDepthTest();
            multibuffersource$buffersource.endBatch();
            RenderSystem.enableDepthTest();
            if(flag) Lighting.setupFor3DItems();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        public static void renderFloatingItemModelIntoGUI(GuiGraphics gui, ItemStack stack, float x, float y, float ticks, float ticksUp){
            Minecraft minecraft = Minecraft.getInstance();
            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
            CustomItemRenderer customItemRenderer = getCustomItemRenderer();

            float old = bakedmodel.getTransforms().gui.rotation.y;
            blitOffset += 50.0F;

            PoseStack poseStack = gui.pose();

            poseStack.pushPose();
            poseStack.translate(x + 8, y + 8, 100 + blitOffset);
            poseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
            poseStack.scale(16.0F, 16.0F, 16.0F);
            poseStack.translate(0.0D, Math.sin(Math.toRadians(ticksUp)) * 0.03125F, 0.0D);
            if(bakedmodel.usesBlockLight()){
                bakedmodel.getTransforms().gui.rotation.y = ticks;
            }else{
                poseStack.mulPose(Axis.YP.rotationDegrees(ticks));
            }
            boolean flag = !bakedmodel.usesBlockLight();
            if(flag) Lighting.setupForFlatItems();

            customItemRenderer.renderItem(stack, ItemDisplayContext.GUI, false, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);

            RenderSystem.disableDepthTest();
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            RenderSystem.enableDepthTest();
            if(flag) Lighting.setupFor3DItems();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();

            bakedmodel.getTransforms().gui.rotation.y = old;
            blitOffset -= 50.0F;
        }

        public static void renderCustomModel(ModelResourceLocation model, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
            BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(model);
            Minecraft.getInstance().getItemRenderer().render(new ItemStack(net.minecraft.world.item.Items.DIRT), displayContext, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedmodel);
        }

        public static void renderBlockModel(ModelResourceLocation model, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
            BakedModel bakedmodel = Minecraft.getInstance().getModelManager().getModel(model);
            Minecraft.getInstance().getItemRenderer().render(new ItemStack(net.minecraft.world.item.Items.DIRT), displayContext, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedmodel);
        }

        public static TextureAtlasSprite getBlockSprite(ResourceLocation resourceLocation) {
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourceLocation);
        }

        public static TextureAtlasSprite getBlockSprite(String modId, String sprite) {
            return getBlockSprite(new ResourceLocation(modId, sprite));
        }

        public static TextureAtlasSprite getParticleSprite(ResourceLocation resourceLocation) {
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_PARTICLES).apply(resourceLocation);
        }

        public static TextureAtlasSprite getParticleSprite(String modId, String sprite) {
            return getParticleSprite(new ResourceLocation(modId, sprite));
        }

        public static TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourceLocation);
        }

        public static TextureAtlasSprite getSprite(String modId, String sprite) {
            return getSprite(new ResourceLocation(modId, sprite));
        }

        public static void renderFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, boolean flowing, int light){
            renderFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, flowing, light);
        }

        public static void renderFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, Col color, boolean flowing, int light){
            renderFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, color, flowing, light);
        }

        public static void renderFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, boolean flowing, int light){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.renderCube(stack, width, height, length);
            }
        }

        public static void renderFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, Col color, boolean flowing, int light){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.setColor(color).renderCube(stack, width, height, length);
            }
        }

        public static void renderCenteredFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, boolean flowing, int light){
            renderCenteredFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, flowing, light);
        }

        public static void renderCenteredFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, Col color, boolean flowing, int light){
            renderCenteredFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, color, flowing, light);
        }

        public static void renderCenteredFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, boolean flowing, int light){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.renderCenteredCube(stack, width, height, length);
            }
        }

        public static void renderCenteredFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, Col color, boolean flowing, int light){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.setColor(color).renderCenteredCube(stack, width, height, length);
            }
        }

        public static void renderWavyFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, boolean flowing, int light, float strength, float time){
            renderWavyFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, flowing, light, strength, time);
        }

        public static void renderWavyFluid(PoseStack stack, FluidStack fluidStack, float size, float texSize, Col color, boolean flowing, int light, float strength, float time){
            renderWavyFluid(stack, fluidStack, size, size, size, texSize, texSize, texSize, color, flowing, light, strength, time);
        }

        public static void renderWavyFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, boolean flowing, int light, float strength, float time){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.renderWavyCube(stack, width, height, length, strength, time);
            }
        }

        public static void renderWavyFluid(PoseStack stack, FluidStack fluidStack, float width, float height, float length, float texWidth, float texHeight, float texLength, Col color, boolean flowing, int light, float strength, float time){
            if(!fluidStack.isEmpty()){
                RenderBuilder builder = getFluidRenderBuilder(fluidStack, texWidth, texHeight, texLength, flowing, light);
                builder.setColor(color).renderWavyCube(stack, width, height, length, strength, time);
            }
        }

        public static RenderBuilder getFluidRenderBuilder(FluidStack fluidStack, float texWidth, float texHeight, float texLength, boolean flowing, int light){
            RenderBuilder builder = RenderBuilder.create().setRenderType(TridotRenderTypes.TRANSLUCENT_TEXTURE);
            if(!fluidStack.isEmpty()){
                FluidType type = fluidStack.getFluid().getFluidType();
                IClientFluidTypeExtensions clientType = IClientFluidTypeExtensions.of(type);
                TextureAtlasSprite sprite = Render.getSprite(clientType.getStillTexture(fluidStack));
                if(flowing) sprite = Render.getSprite(clientType.getFlowingTexture(fluidStack));

                builder.setFirstUV(sprite.getU0(), sprite.getV0(), sprite.getU0() + ((sprite.getU1() - sprite.getU0()) * texLength), sprite.getV0() + ((sprite.getV1() - sprite.getV0()) * texWidth))
                .setSecondUV(sprite.getU0(), sprite.getV0(), sprite.getU0() + ((sprite.getU1() - sprite.getU0()) * texWidth), sprite.getV0() + ((sprite.getV1() - sprite.getV0()) * texHeight))
                .setThirdUV(sprite.getU0(), sprite.getV0(), sprite.getU0() + ((sprite.getU1() - sprite.getU0()) * texLength), sprite.getV0() + ((sprite.getV1() - sprite.getV0()) * texHeight))
                .setColor(new Col(clientType.getTintColor(fluidStack)))
                .setLight(Math.max(type.getLightLevel(fluidStack) << 4, light));
            }
            return builder;
        }

        public static void renderConnectLine(PoseStack stack, Vec3 from, Vec3 to, Col color, float alpha){
            double dX = to.x() - from.x();
            double dY = to.y() - from.y();
            double dZ = to.z() - from.z();

            double yaw = Math.atan2(dZ, dX);
            double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

            stack.pushPose();
            stack.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(-yaw)));
            stack.mulPose(Axis.ZP.rotationDegrees((float)Math.toDegrees(-pitch) - 180f));
            RenderBuilder.create().setRenderType(TridotRenderTypes.ADDITIVE)
            .setColor(color)
            .setAlpha(alpha)
            .renderRay(stack, 0.01f, (float)from.distanceTo(to) + 0.01f);
            stack.popPose();
        }

        public static void renderConnectLine(PoseStack stack, BlockPos posFrom, BlockPos posTo, Col color, float alpha){
            renderConnectLine(stack, posFrom.getCenter(), posTo.getCenter(), color, alpha);
        }

        public static void renderConnectLineOffset(PoseStack stack, Vec3 from, Vec3 to, Col color, float alpha){
            stack.pushPose();
            stack.translate(from.x(), from.y(), from.z());
            renderConnectLine(stack, from, to, color, alpha);
            stack.popPose();
        }

        public static void renderConnectBoxLines(PoseStack stack, Vec3 size, Col color, float alpha){
            renderConnectLineOffset(stack, new Vec3(0, 0, 0), new Vec3(size.x(), 0, 0), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, 0), new Vec3(size.x(), 0, size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, size.z()), new Vec3(0, 0, size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(0, 0, size.z()), new Vec3(0, 0, 0), color, alpha);

            renderConnectLineOffset(stack, new Vec3(0, 0, 0), new Vec3(0, size.y(), 0), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, 0), new Vec3(size.x(), size.y(), 0), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, size.z()), new Vec3(size.x(), size.y(), size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(0, 0, size.z()), new Vec3(0, size.y(), size.z()), color, alpha);

            renderConnectLineOffset(stack, new Vec3(0, size.y(), 0), new Vec3(size.x(), size.y(), 0), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), size.y(), 0), new Vec3(size.x(), size.y(), size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), size.y(), size.z()), new Vec3(0, size.y(), size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(0, size.y(), size.z()), new Vec3(0, size.y(), 0), color, alpha);
            stack.pushPose();
            stack.translate(0.01f, 0.01f, 0.01f);
            RenderBuilder.create().setRenderType(TridotRenderTypes.ADDITIVE)
            .setColor(color)
            .setAlpha(alpha / 8f)
            .enableSided()
            .renderCube(stack, (float)size.x() - 0.02f, (float)size.y() - 0.02f, (float)size.z() - 0.02f);
            stack.popPose();
        }

        public static void renderConnectSideLines(PoseStack stack, Vec3 size, Col color, float alpha){
            renderConnectLineOffset(stack, new Vec3(0, 0, 0), new Vec3(size.x(), 0, 0), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, 0), new Vec3(size.x(), 0, size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(size.x(), 0, size.z()), new Vec3(0, 0, size.z()), color, alpha);
            renderConnectLineOffset(stack, new Vec3(0, 0, size.z()), new Vec3(0, 0, 0), color, alpha);
            stack.pushPose();
            stack.mulPose(Axis.XP.rotationDegrees(90f));
            RenderBuilder.create().setRenderType(TridotRenderTypes.ADDITIVE)
            .setColor(color)
            .setAlpha(alpha / 8f)
            .enableSided()
            .renderQuad(stack, (float)size.x(), (float)size.y());
            stack.popPose();
        }

        public static void renderConnectSide(PoseStack stack, Direction side, Col color, float alpha){
            Vec3 size = new Vec3(1, 1, 1);
            stack.pushPose();
            stack.translate(0.5f, 0.5f, 0.5f);
            stack.mulPose(side.getOpposite().getRotation());
            stack.translate(0, -0.001f, 0);
            stack.translate(-size.x() / 2f, -size.y() / 2f, -size.z() / 2f);
            renderConnectSideLines(stack, size, color, alpha);
            stack.popPose();
        }

        public static boolean isFormulaLine(double f, double j, boolean limit, double l){
            if(limit){
                return f >= j - l && f <= j + l;
            }
            return false;
        }

        public static Vector3f parametricSphere(float u, float v, float r){
            return new Vector3f(Mth.cos(u) * Mth.sin(v) * r, Mth.cos(v) * r, Mth.sin(u) * Mth.sin(v) * r);
        }

        public static Vec2 perpendicularTrailPoints(Vector4f start, Vector4f end, float width){
            float x = -start.x();
            float y = -start.y();
            if(Math.abs(start.z()) > 0){
                float ratio = end.z() / start.z();
                x = end.x() + x * ratio;
                y = end.y() + y * ratio;
            }else if(Math.abs(end.z()) <= 0){
                x += end.x();
                y += end.y();
            }
            if(start.z() > 0){
                x = -x;
                y = -y;
            }
            if(x * x + y * y > 0F){
                float normalize = width * 0.5F / distance(x, y);
                x *= normalize;
                y *= normalize;
            }
            return new Vec2(-y, x);
        }

        public static float distance(float... a){
            return sqrt(distSqr(a));
        }

        public static float distSqr(float... a){
            float d = 0.0F;
            for(float f : a){
                d += f * f;
            }
            return d;
        }

        public static void applyWobble(Vector3f[] offsets, float strength, float gameTime){
            float offset = 0;
            for(Vector3f vector3f : offsets){
                double time = ((gameTime / 40.0F) % Math.PI * 2);
                float sine = Mth.sin((float)(time + (offset * Math.PI * 2))) * strength;
                vector3f.add(sine, -sine, 0);
                offset += 0.25f;
            }
        }
    }

    /**Level utils*/
    public static class Levels {
        public static ServerLevel overworld() {
            return server().overworld();
        }
        public static ServerLevel nether() {
            return server().getLevel(Level.NETHER);
        }
        public static ServerLevel end() {
            return server().getLevel(Level.END);
        }
    }
}
