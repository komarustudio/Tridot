package pro.komaru.tridot.api.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pro.komaru.tridot.Tridot;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Should be added to your BossEntity implementing class
 * <pre><code>
 *     public final List<UUID> nearbyPlayers = new ArrayList<>();
 *     public final Map<UUID, Float> damageMap = new HashMap<>();
 *
 *     public void readAdditionalSaveData(CompoundTag pCompound) {
 *         super.readAdditionalSaveData(pCompound);
 *         readBossData(pCompound);
 *     }
 *
 *     public void addAdditionalSaveData(CompoundTag pCompound) {
 *         super.addAdditionalSaveData(pCompound);
 *         saveBossData(pCompound);
 *     }
 *
 *     public void onAddedToWorld() {
 *         super.onAddedToWorld();
 *         CompoundTag data = this.getPersistentData();
 *         if (!data.getBoolean("NearbyPlayerHealthBoost")){
 *             initializeNearbyPlayers(this.level(), this);
 *             applyHealthBoost(this);
 *         }
 *     }
 *
 *     public boolean hurt(DamageSource source, float amount) {
 *         if (source.getDirectEntity() instanceof Player player) {
 *             UUID playerUUID = player.getUUID();
 *             getDamageMap().put(playerUUID, getDamageMap().getOrDefault(playerUUID, 0.0f) + amount);
 *         }
 *
 *         return super.hurt(source, amount);
 *     }
 *
 *    {@literal @}Nullable
 *     public ItemEntity spawnAtLocation(ItemStack stack, float offsetY) {
 *         if (stack.isEmpty() || this.level().isClientSide) return null;
 *         initializeLoot(this.level(), stack, this.getOnPos(), offsetY);
 *         return null;
 *     }</code></pre>
 */
public interface BossEntity{
    List<UUID> getNearbyPlayers();

    Map<UUID, Float> getDamageMap();

    default void readBossData(CompoundTag pCompound){
        getNearbyPlayers().clear();
        getDamageMap().clear();
        ListTag nearbyPlayersList = pCompound.getList("NearbyPlayers", Tag.TAG_COMPOUND);
        for(int i = 0; i < nearbyPlayersList.size(); i++){
            CompoundTag playerTag = nearbyPlayersList.getCompound(i);
            UUID playerUUID = playerTag.getUUID("UUID");
            getNearbyPlayers().add(playerUUID);
        }

        ListTag damageMapList = pCompound.getList("DamageMap", Tag.TAG_COMPOUND);
        for(int i = 0; i < damageMapList.size(); i++){
            CompoundTag damageTag = damageMapList.getCompound(i);
            UUID playerUUID = damageTag.getUUID("UUID");
            float damage = damageTag.getFloat("Damage");
            getDamageMap().put(playerUUID, damage);
        }
    }

    default void saveBossData(CompoundTag pCompound){
        ListTag nearbyPlayersList = new ListTag();
        for(UUID playerUUID : getNearbyPlayers()){
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", playerUUID);
            nearbyPlayersList.add(playerTag);
        }

        pCompound.put("NearbyPlayers", nearbyPlayersList);
        ListTag damageMapList = new ListTag();
        for(Map.Entry<UUID, Float> entry : getDamageMap().entrySet()){
            CompoundTag damageTag = new CompoundTag();
            damageTag.putUUID("UUID", entry.getKey());
            damageTag.putFloat("Damage", entry.getValue());
            damageMapList.add(damageTag);
        }

        pCompound.put("DamageMap", damageMapList);
    }

    default int getRadius(){
        return 16;
    }

    default float scalingFactor(float a, float b, float playerCount) {
        float x = playerCount - 1;
        return a * x * x + b * x + 1;
    }

    /**
     * {@code a} is the scaling boost per additional player
     * {@code b} is the base scaling factor
     */
    float a = 0.1f, b = 0.1f;
    default int getHealthScale(Mob mob) {
        int defaultHealth = (int)mob.getMaxHealth();
        int playerCount = getNearbyPlayers().size() - 1;

        return (int)(defaultHealth * scalingFactor(a,b,playerCount));
    }

    default double getRequiredDamage(){
        return 50;
    }

    default void initializeLoot(Level level, ItemStack stack, BlockPos pos, float offsetY){
        for(UUID playerUUID : getNearbyPlayers()){
            if(getDamageMap().getOrDefault(playerUUID, 0.0f) >= getRequiredDamage()){
                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + offsetY, pos.getZ(), stack.copy());
                itemEntity.setExtendedLifetime();
                itemEntity.setInvulnerable(true);
                itemEntity.setGlowingTag(true);
                itemEntity.setDefaultPickUpDelay();
                itemEntity.setTarget(playerUUID);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    default void initializeNearbyPlayers(Level level, Entity entity){
        List<Player> players = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(getRadius()));
        for(Player player : players){
            getNearbyPlayers().add(player.getUUID());
            getDamageMap().put(player.getUUID(), 0.0f);
        }
    }

    ResourceLocation NEARBY_PLAYER_BONUS_ID = Tridot.ofTridot("nearby_player_bonus");
    default void applyBonusHealth(Mob mob){
        if(mob.getAttribute(Attributes.MAX_HEALTH) != null && getNearbyPlayers().size() > 1){
            ResourceLocation healthModifierId = NEARBY_PLAYER_BONUS_ID;
            AttributeModifier existingModifier = mob.getAttribute(Attributes.MAX_HEALTH).getModifier(healthModifierId);
            if(existingModifier != null){
                mob.getAttribute(Attributes.MAX_HEALTH).removeModifier(existingModifier);
            }

            AttributeModifier healthModifier = new AttributeModifier(healthModifierId, getHealthScale(mob), AttributeModifier.Operation.ADD_VALUE);
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(healthModifier);
            mob.setHealth((float)mob.getAttribute(Attributes.MAX_HEALTH).getValue());
            mob.getPersistentData().putBoolean("NearbyPlayerHealthBonus", true);
        }
    }
}