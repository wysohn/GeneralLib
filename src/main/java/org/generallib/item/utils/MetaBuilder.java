package org.generallib.item.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MetaBuilder {
    private ItemMeta IM;

    private String title;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();

    public MetaBuilder(Material type) {
        super();
        IM = Bukkit.getItemFactory().getItemMeta(type);
    }

    public MetaBuilder withCustomMeta(MetaInjector injector) {
        IM = injector.inject(IM);
        return this;
    }

    public MetaBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public MetaBuilder withLore(int index, String value) {
        if(index < lore.size()) {
            lore.set(Math.max(0, index), value);
        }else {
            while(index > lore.size()) {
                lore.add("");
            }
            lore.add(value);
        }

        return this;
    }

    public MetaBuilder withEnchantment(Enchantment ench, int level) {
        this.enchantments.put(ench, level);
        return this;
    }

    void apply(ItemStack IS) {
        if(IM != null) {
            IM.setDisplayName(title);
            IM.setLore(lore);
            for(Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
                IM.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        IS.setItemMeta(IM);
    }

    public interface MetaInjector{
        ItemMeta inject(ItemMeta meta);
    }
}
