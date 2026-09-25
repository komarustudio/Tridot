package pro.komaru.tridot.common.registry.item.types;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

import java.util.*;

// PORT NOTE: the "Items" NBT list is now the vanilla minecraft:container component (ItemContainerContents).
public class ItemBackedInventory extends SimpleContainer{
    private final ItemStack stack;

    public ItemBackedInventory(ItemStack stack, int expectedSize){
        super(expectedSize);
        this.stack = stack;

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> lst = NonNullList.withSize(Math.max(expectedSize, contents.getSlots()), ItemStack.EMPTY);
        contents.copyInto(lst);
        for(int i = 0; i < expectedSize && i < lst.size(); i++){
            setItem(i, lst.get(i));
        }
    }

    @Override
    public boolean stillValid(Player player){
        return !stack.isEmpty();
    }

    @Override
    public void setChanged(){
        super.setChanged();
        List<ItemStack> list = new ArrayList<>();
        for(int i = 0; i < getContainerSize(); i++){
            list.add(getItem(i));
        }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
    }
}
