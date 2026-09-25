package pro.komaru.tridot.common;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.api.events.CalculatePercentArmorEvent;
import pro.komaru.tridot.client.sound.MusicHandler;
import pro.komaru.tridot.client.sound.MusicModifier;
import pro.komaru.tridot.common.config.CommonConfig;
import pro.komaru.tridot.common.registry.TagsRegistry;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.armor.*;
import pro.komaru.tridot.common.registry.item.types.*;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.common.networking.packets.DungeonSoundPacket;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.util.*;

import java.util.*;
import java.util.stream.*;

public class Events{
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onJoinServer(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if(!event.isCanceled() && entity instanceof ServerPlayer player){
            evaluateArmorEffects(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        evaluateArmorEffects(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        evaluateArmorEffects(event.getEntity());
    }

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        if(slot.isArmor() && entity instanceof Player player) {
            evaluateArmorEffects(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post e) {
        Player player = e.getEntity();
        if(!player.level().isClientSide()){
            evaluateArmorEffects(player);
        }
    }

    @SubscribeEvent
    public void onShieldBlock(LivingShieldBlockEvent ev) {
        LivingEntity entity = ev.getEntity();
        ItemStack stack = entity.getUseItem();
        if (stack.getItem() instanceof ConfiguredShield shieldItem) {
            float armor = shieldItem.onPostBlock(ev.getDamageSource(), ev.getOriginalBlockedDamage(), stack, entity, shieldItem.builder.blockedPercent);

            if (armor == 0) {
                ev.setBlockedDamage(0);
                return;
            }

            if (shieldItem.builder.canParry) {
                int ticksUsing = entity.getTicksUsingItem();
                int parryWindow = shieldItem.getParryWindow(stack);
                if(ev.getDamageSource().is(TagsRegistry.BYPASSES_PARRY)) return;

                if (ticksUsing <= parryWindow) {
                    shieldItem.onParry(ev.getDamageSource(), ev.getOriginalBlockedDamage(), stack, entity);
                    ev.setBlockedDamage(ev.getOriginalBlockedDamage());
                    return;
                }
            }

            float blockMultiplier = Math.max(Math.min(armor, 1.0F), 0.0F);
            float blockedDamage = ev.getOriginalBlockedDamage() * blockMultiplier;

            shieldItem.onShieldBlock(ev.getDamageSource(), ev.getOriginalBlockedDamage(), stack, entity);
            ev.setBlockedDamage(blockedDamage);

            if (blockedDamage < 1) {
                var sound = shieldItem.builder.blockSound;
                if (sound != null) entity.playSound(sound, 1.0F, 0.8F + Tmp.rnd.nextFloat() * 0.4F);
            }
        }
    }

    public void evaluateArmorEffects(Player player) {
        Set<Holder<MobEffect>> currentlyApplied = getTrackedEffects(player);
        Set<Holder<MobEffect>> newApplied = new HashSet<>();
        for (var entry : AbstractArmorRegistry.EFFECTS.entrySet()) {
            var material = entry.getKey();
            if (SuitArmorItem.hasCorrectArmorOn(material, player)) {
                for (var effectData : entry.getValue()) {
                    if (effectData.condition().test(player)) {
                        Holder<MobEffect> effect = effectData.instance().get().getEffect();
                        newApplied.add(effect);
                        if (!player.hasEffect(effect)) {
                            MobEffectInstance instance = effectData.instance().get();
                            player.addEffect(instance);
                        }
                    }
                }
            }
        }

        for (Holder<MobEffect> oldEffect : currentlyApplied) {
            if (!newApplied.contains(oldEffect) && player.hasEffect(oldEffect)) {
                player.removeEffect(oldEffect);
            }
        }

        saveTrackedEffects(player, newApplied);
    }

    private static final String ARMOR_EFFECTS_TAG = "ArmorEffects";
    public Set<Holder<MobEffect>> getTrackedEffects(Player player) {
        CompoundTag tag = player.getPersistentData().getCompound(ARMOR_EFFECTS_TAG);
        Set<Holder<MobEffect>> effects = new HashSet<>();
        for (String key : tag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) BuiltInRegistries.MOB_EFFECT.getHolder(id).ifPresent(effects::add);
        }

        return effects;
    }

    public void saveTrackedEffects(Player player, Set<Holder<MobEffect>> effects) {
        CompoundTag tag = new CompoundTag();
        for (Holder<MobEffect> effect : effects) {
            tag.putBoolean(effect.unwrapKey().map(k -> k.location().toString()).orElseGet(() -> BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString()), true);
        }

        player.getPersistentData().put(ARMOR_EFFECTS_TAG, tag);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 100 != 0) return;
        for (Player player : server.getPlayerList().getPlayers()) {
            for(MusicModifier modifier : MusicHandler.getModifiers()) {
                if(modifier instanceof MusicModifier.DungeonMusic dungeonMusic) {
                    boolean clientHasMusic = FMLEnvironment.dist.isClient() && TridotLibClient.DUNGEON_MUSIC_INSTANCE != null;
                    if (dungeonMusic.isPlayerInStructure(player, (ServerLevel) player.level()) && !clientHasMusic) PacketHandler.sendTo(player, new DungeonSoundPacket(dungeonMusic.music, player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ()));
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        Entity target = event.getTarget();
        Player attacker = event.getEntity();
        if (!(target instanceof LivingEntity living)) return;
        for (var entry : AbstractArmorRegistry.HIT_EFFECTS.entrySet()) {
            var material = entry.getKey();
            if (!SuitArmorItem.hasCorrectArmorOn(material, attacker)) return;
            for (var effectData : entry.getValue()) {
                float chance = effectData.chance();
                if (!Tmp.rnd.chance(chance) || !effectData.condition().test(attacker)) continue;
                living.addEffect(effectData.instance().get());
            }
        }
    }

    @SubscribeEvent
    public void disableBlock(LivingShieldBlockEvent event){
        if(event.getDamageSource().getDirectEntity() instanceof Player player){
            LivingEntity mob = event.getEntity();
            ItemStack weapon = player.getMainHandItem();
            if(!weapon.isEmpty() && weapon.is(TagsRegistry.CAN_DISABLE_SHIELD) && mob instanceof Player attacked){
                attacked.disableShield();
            }
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent e){
        ItemStack itemStack = e.getItemStack();
        Utils.Items.addSkinTooltip(itemStack, e.getToolTip());
        if(Utils.isDevelopment){
            Stream<ResourceLocation> itemTagStream = itemStack.getTags().map(TagKey::location);
            if(Minecraft.getInstance().options.advancedItemTooltips){
                if(Screen.hasControlDown()){
                    if(!itemStack.getTags().toList().isEmpty()){
                        e.getToolTip().add(Component.empty());
                        e.getToolTip().add(Component.literal("ItemTags: " + itemTagStream.toList()).withStyle(ChatFormatting.DARK_GRAY));
                    }

                    if(itemStack.getItem() instanceof BlockItem blockItem){
                        BlockState blockState = blockItem.getBlock().defaultBlockState();
                        Stream<ResourceLocation> blockTagStream = blockState.getTags().map(TagKey::location);
                        if(!blockState.getTags().map(TagKey::location).toList().isEmpty()){
                            if(itemStack.getTags().toList().isEmpty()){
                                e.getToolTip().add(Component.empty());
                            }

                            e.getToolTip().add(Component.literal("BlockTags: " + blockTagStream.toList()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                }else if(!itemStack.getTags().toList().isEmpty() || itemStack.getItem() instanceof BlockItem blockItem && !blockItem.getBlock().defaultBlockState().getTags().toList().isEmpty()){
                    e.getToolTip().add(Component.empty());
                    e.getToolTip().add(Component.literal("Press [Control] to get tags info").withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            float incomingDamage = event.getAmount();
            if (CommonConfig.PERCENT_ARMOR.get()) {
                if (event.getEntity().getAttribute(AttributeRegistry.PERCENT_ARMOR) == null) return;
                float armor = (float) event.getEntity().getAttributeValue(AttributeRegistry.PERCENT_ARMOR) / 100;
                float baseMultiplier = Math.max(Math.min(1 - armor, 1), 0);
                var calcEvent = new CalculatePercentArmorEvent(event.getEntity(), incomingDamage, baseMultiplier);
                if (NeoForge.EVENT_BUS.post(calcEvent).isCanceled()) {
                    return;
                }

                float finalMultiplier = calcEvent.getMultiplier();
                float reducedDamage = incomingDamage * finalMultiplier;
                event.setAmount(reducedDamage);
            }
        }
    }}
