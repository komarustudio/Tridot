package pro.komaru.tridot.util.file.parsers;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

public class TypeAdapters {
    public static JsonDeserializer<ItemStack> itemStackDeserializer() {
        return (json, typeOfT, context) -> {
            if(json.isJsonPrimitive() && ((JsonPrimitive) json).isString()) {
                String[] splits = json.getAsString().split(" ");
                String itemLocation = splits[0];
                int itemCount = 1;
                if(splits.length == 2) {
                    itemCount = Integer.parseInt(splits[0]);
                    itemLocation = splits[1];
                }
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemLocation));
                return new ItemStack(item,itemCount);
            }
            JsonObject object = json.getAsJsonObject();
            ResourceLocation itemLocation = context.deserialize(object.get("item"), ResourceLocation.class);
            CompoundTag nbt = new CompoundTag();
            int itemCount = 1;
            if(object.has("count")) itemCount = context.deserialize(object.get("count"),Integer.class);
            if(object.has("nbt")) {
                String nbtObject = object.get("nbt").toString();
                try {
                    nbt = TagParser.parseTag(nbtObject);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }

            }
            Item item = BuiltInRegistries.ITEM.get(itemLocation);
            ItemStack stack = new ItemStack(item,itemCount);
            if(!nbt.isEmpty()) stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return stack;
        };
    }
    public static JsonDeserializer<ResourceLocation> resourceLocationDeserializer() {
        return (json, typeOfT, context) -> {
            String id = json.getAsString();
            return ResourceLocation.parse(id);
        };
    }

}
