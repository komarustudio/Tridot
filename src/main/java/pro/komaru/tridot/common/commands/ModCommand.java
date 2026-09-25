package pro.komaru.tridot.common.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.commands.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import pro.komaru.tridot.api.command.*;
import pro.komaru.tridot.common.commands.parts.*;
import pro.komaru.tridot.common.registry.item.skins.*;
import pro.komaru.tridot.util.struct.data.*;

import java.util.*;

public class ModCommand{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        CommandArgument targets = CommandArgument.entities("targets");
        CommandArguments skinArg = CommandArguments.skin("skin");
        CommandBuilder skin = new CommandBuilder("skin").permission((p) -> p.hasPermission(2));
        CommandBuilder builder = new CommandBuilder("tridot").variants(
        skin.variants(
                        new CommandVariant(CommandPart.create("canApply"), targets).execute((p) -> {
                            canApply(p.getSource(), targets.getPlayers(p));
                            return 1;
                        }),

                        new CommandVariant(CommandPart.create("apply"), targets, skinArg).execute((p) -> {
                            applySkin(p.getSource(), targets.getPlayers(p), skinArg.getSkin(p, "skin"));
                            return 1;
                        }),

                        new CommandVariant(CommandPart.create("remove"), targets).execute((p) -> {
                            remove(p.getSource(), targets.getPlayers(p));
                            return 1;
                        })
         )
        );

        dispatcher.register(builder.permission((p) -> p.hasPermission(2)).build());
    }

    private static void applySkin(CommandSourceStack command, Collection<? extends ServerPlayer> targetPlayers, ItemSkin itemSkin) throws CommandSyntaxException{
        for(ServerPlayer player : targetPlayers){
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                if (itemSkin.appliesOn(stack)) {
                    itemSkin.apply(stack);
                    if(targetPlayers.size() == 1){
                        command.sendSuccess(() -> Component.translatable("commands.tridot.skin.applied.single", targetPlayers.iterator().next().getDisplayName()), true);
                    }else{
                        command.sendSuccess(() -> Component.translatable("commands.tridot.skin.applied.multiple", targetPlayers.size()), true);
                    }

                } else {
                    command.sendFailure(Component.translatable("commands.tridot.skin.fail"));
                }
            }
        }
    }

    private static void canApply(CommandSourceStack command, Collection<? extends ServerPlayer> targetPlayers) throws CommandSyntaxException{
        for(ServerPlayer player : targetPlayers){
            ItemStack stack = player.getMainHandItem();
            if(!stack.isEmpty()){
                List<String> skinIds = new ArrayList<>();
                for(ItemSkin skin : SkinRegistryManager.getApplicableSkins(stack)){
                    skinIds.add(String.valueOf(skin.id()));
                }

                String formattedSkins = String.join(", ", skinIds);
                command.sendSuccess(() -> Component.literal(formattedSkins), true);

            }else{
                command.sendFailure(Component.translatable("commands.tridot.skin.fail"));
            }
        }
    }

    private static void remove(CommandSourceStack command, Collection<? extends ServerPlayer> targetPlayers) throws CommandSyntaxException{
        for(ServerPlayer player : targetPlayers){
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                ItemSkin itemSkin = ItemSkin.itemSkin(stack);
                if (itemSkin != null) {
                    ItemSkin.remove(stack);
                }
            }

            if(targetPlayers.size() == 1){
                command.sendSuccess(() -> Component.translatable("commands.tridot.skin.remove.single", targetPlayers.iterator().next().getDisplayName()), true);
            }else{
                command.sendSuccess(() -> Component.translatable("commands.tridot.skin.remove.multiple", targetPlayers.size()), true);
            }
        }
    }

}