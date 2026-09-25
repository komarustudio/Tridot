package pro.komaru.tridot.api.entity.trades;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.saveddata.maps.*;
import net.neoforged.neoforge.common.*;

import javax.annotation.*;

public class TreasureMapItemListing extends BasicItemListing{
    private final int emeraldCost;
    private final TagKey<Structure> destination;
    private final String displayName;
    private final Holder<MapDecorationType> destinationType;
    private final int maxUses;
    private final int villagerXp;

    public TreasureMapItemListing(int pEmeraldCost, TagKey<Structure> pDestination, String pDisplayName, Holder<MapDecorationType> pDestinationType, int pMaxUses, int pVillagerXp) {
        super(pEmeraldCost, new ItemStack(Items.COMPASS), pMaxUses, pVillagerXp, 0);
        this.emeraldCost = pEmeraldCost;
        this.destination = pDestination;
        this.displayName = pDisplayName;
        this.destinationType = pDestinationType;
        this.maxUses = pMaxUses;
        this.villagerXp = pVillagerXp;
    }

    public TreasureMapItemListing(Trade trade, int pEmeraldCost, TagKey<Structure> pDestination, String pDisplayName, Holder<MapDecorationType> pDestinationType) {
        super(pEmeraldCost, new ItemStack(Items.COMPASS), trade.maxTrades, trade.experience, trade.multiplier);
        this.emeraldCost = pEmeraldCost;
        this.destination = pDestination;
        this.displayName = pDisplayName;
        this.destinationType = pDestinationType;
        this.maxUses = trade.maxTrades;
        this.villagerXp = trade.experience;
    }

    @Nullable
    public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
        if (!(pTrader.level() instanceof ServerLevel serverlevel)) {
            return null;
        } else {
            BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, pTrader.blockPosition(), 100, true);
            if (blockpos != null) {
                ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte)2, true, true);
                MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.destinationType);
                itemstack.set(DataComponents.CUSTOM_NAME, Component.translatable(this.displayName));
                return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), java.util.Optional.of(new ItemCost(Items.COMPASS)), itemstack, this.maxUses, this.villagerXp, 0.2F);
            } else {
                return null;
            }
        }
    }
}
