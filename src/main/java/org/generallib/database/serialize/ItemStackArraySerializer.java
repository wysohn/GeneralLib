package org.generallib.database.serialize;

import java.lang.reflect.Type;

import org.bukkit.inventory.ItemStack;

import copy.com.google.gson.JsonArray;
import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;

public class ItemStackArraySerializer implements Serializer<ItemStack[]> {

    @Override
    public JsonElement serialize(ItemStack[] src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray arr = new JsonArray();
        for(ItemStack IS : src) {
            arr.add(context.serialize(IS, ItemStack.class));
        }
        return arr;
    }

    @Override
    public ItemStack[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray arr = (JsonArray) json;
        ItemStack[] src = new ItemStack[arr.size()];
        for(int i = 0; i < src.length; i++) {
            src[i] = context.deserialize(arr.get(i), ItemStack.class);
        }

        return src;
    }

}
