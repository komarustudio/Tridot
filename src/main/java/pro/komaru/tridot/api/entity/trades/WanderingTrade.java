package pro.komaru.tridot.api.entity.trades;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.world.entity.npc.VillagerTrades.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.*;

import java.util.*;

public class WanderingTrade extends Trade{
    private final List<ItemListing> trades;
    public WanderingTrade(List<ItemListing> trades) {
        super(new Int2ObjectArrayMap<>(0));
        this.trades = trades;
    }

    /**
     * Player offer for villager
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createSale(int pEmeralds, ItemLike item){
        trades.add(new BasicItemListing(new ItemStack(item, 1), new ItemStack(Items.EMERALD, pEmeralds), maxTrades, experience, multiplier));
    }

    /**
     * Player offer for villager
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createSale(int pEmeralds, ItemLike item, int count){
        trades.add(new BasicItemListing(new ItemStack(item, count), new ItemStack(Items.EMERALD, pEmeralds), maxTrades, experience, multiplier));
    }

    /**
     * Villager offer for Emeralds
     * @param pEmeralds Price
     * @param additional Additional item
     * @param item Item for sale
     */
    public void createOffer(int pEmeralds, ItemLike additional, ItemLike item){
        trades.add(new BasicItemListing(new ItemStack(Items.EMERALD, pEmeralds), new ItemStack(additional), new ItemStack(item), maxTrades, experience, multiplier));
    }

    /**
     * Villager offer for Emeralds
     * @param pEmeralds Price
     * @param item Item for sale
     */
    public void createOffer(int pEmeralds, ItemLike item){
        trades.add(new BasicItemListing(pEmeralds, new ItemStack(item), maxTrades, experience, multiplier));
    }

    public void createListing(BasicItemListing listing){
        trades.add(listing);
    }
}
