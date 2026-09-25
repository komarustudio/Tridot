package pro.komaru.tridot.api.entity.trades;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.world.entity.npc.VillagerTrades.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.*;

import java.util.*;

public class Trade{
    private final Int2ObjectMap<List<ItemListing>> trades;
    public TradeTier tier;
    public float multiplier;
    public int experience;
    public int maxTrades;

    public Trade(Int2ObjectMap<List<ItemListing>> trades) {
        this.trades = trades;
        this.maxTrades = 1;
        this.experience = 1;
        this.multiplier = 0;
        this.tier = TradeTier.NOVICE;
    }

    /**
     * Villager tier needed for this trade
     */
    public Trade tier(TradeTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Max trade count
     */
    public Trade trades(int max) {
        this.maxTrades = max;
        return this;
    }

    /**
     * Experience from trade
     */
    public Trade xp(int xp) {
        this.experience = xp;
        return this;
    }

    /**
     * Price multiplier
     */
    public Trade mul(float mul) {
        this.multiplier = mul;
        return this;
    }

    /**
     * Player offer for villager
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createSale(int pEmeralds, ItemLike item){
        trades.get(tier.getLevel()).add(new BasicItemListing(new ItemStack(item, 1), new ItemStack(Items.EMERALD, pEmeralds), maxTrades, experience, multiplier));
    }

    /**
     * Player offer for villager
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createSale(int pEmeralds, ItemLike item, int count){
        trades.get(tier.getLevel()).add(new BasicItemListing(new ItemStack(item, count), new ItemStack(Items.EMERALD, pEmeralds), maxTrades, experience, multiplier));
    }

    /**
     * Villager offer for Emeralds
     * @param pEmeralds Price
     * @param additional Additional item
     * @param item Item for sale
     */
    public void createOffer(int pEmeralds, ItemLike additional, ItemLike item){
        trades.get(tier.getLevel()).add(new BasicItemListing(new ItemStack(Items.EMERALD, pEmeralds), new ItemStack(additional), new ItemStack(item), maxTrades, experience, multiplier));
    }

    /**
     * Villager offer for Emeralds
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createOffer(int pEmeralds, ItemLike item){
        trades.get(tier.getLevel()).add(new BasicItemListing(pEmeralds, new ItemStack(item), maxTrades, experience, multiplier));
    }

    public void createListing(BasicItemListing listing){
        trades.get(tier.getLevel()).add(listing);
    }

    public enum TradeTier {
        NOVICE(1),
        APPRENTICE(2),
        JOURNEYMAN(3),
        EXPERT(4),
        MASTER(5);

        public final int level;
        TradeTier(int level) {
            this.level = level;
        }

        public int getLevel(){
            return level;
        }
    }
}
