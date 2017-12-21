package org.generallib.item.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemBuilder {
    private final Material material;
    private int amount = 1;
    private short data = 0;
    private MetaBuilder builder;

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder withData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withMetaBuilder(MetaBuilder builder) {
        this.builder = builder;
        return this;
    }

    public ItemStack build() {
        ItemStack IS = new ItemStack(material);
        IS.setAmount(amount);
        IS.setDurability(data);

        if(builder != null) {
            builder.apply(IS);
        }

        return IS;
    }
}
