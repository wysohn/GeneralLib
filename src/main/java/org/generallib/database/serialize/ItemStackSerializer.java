/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.generallib.database.serialize;

import java.lang.reflect.Type;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.generallib.serializetools.Utf8YamlConfiguration;

import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonObject;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;

public class ItemStackSerializer implements Serializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack arg0, Type arg1, JsonSerializationContext arg2) {
        JsonObject obj = new JsonObject();
        FileConfiguration fc = new Utf8YamlConfiguration();
        fc.set("ItemStack", arg0);
        obj.addProperty("IS", fc.saveToString());

        return obj;
    }

    @Override
    public ItemStack deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        JsonObject obj = (JsonObject) arg0;
        FileConfiguration fc = new Utf8YamlConfiguration();
        try {
            fc.loadFromString(obj.get("IS").getAsString());
            return fc.getItemStack("ItemStack");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
