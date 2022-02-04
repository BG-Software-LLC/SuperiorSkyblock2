package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class TemplateItem {

    public static final TemplateItem AIR = new TemplateItem(new ItemBuilder(Material.AIR));

    private final ItemBuilder itemBuilder;

    public TemplateItem(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public ItemBuilder getBuilder() {
        return itemBuilder.copy();
    }

    public ItemBuilder getEditableBuilder() {
        return itemBuilder.copy();
    }

    public ItemStack build() {
        return getBuilder().build();
    }

    public ItemStack build(SuperiorPlayer superiorPlayer) {
        return getBuilder().build(superiorPlayer);
    }

}
