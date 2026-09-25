package pro.komaru.tridot.client.tooltip;

import net.minecraft.core.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;

public class AttributeTooltipModifier{

    public boolean isToolBase(AttributeModifier modifier, Player player, TooltipFlag flag){
        return false;
    }

    public boolean isModifiable(Holder<Attribute> key, AttributeModifier modifier, Player player, TooltipFlag flag){
        return isToolBase(modifier, player, flag);
    }

    public ModifyResult modify(AttributeModifier modifier, double amount, AttributeModifier.Operation operation){
        return new ModifyResult(modifier, amount, operation);
    }

    public static class ModifyResult{
        public AttributeModifier modifier;
        public double amount;
        public AttributeModifier.Operation operation;

        public ModifyResult(AttributeModifier modifier, double amount, AttributeModifier.Operation operation){
            this.modifier = modifier;
            this.amount = amount;
            this.operation = operation;
        }

        public AttributeModifier getModifier(){
            return modifier;
        }

        public double getAmount(){
            return amount;
        }

        public AttributeModifier.Operation getOperation(){
            return operation;
        }

        public AttributeModifier toModifier(){
            return new AttributeModifier(modifier.id(), amount, operation);
        }
    }
}
