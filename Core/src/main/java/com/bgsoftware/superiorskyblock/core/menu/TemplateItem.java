package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TemplateItem {

    public static final TemplateItem AIR = new TemplateItem(new ItemBuilder(Material.AIR));

    private final ItemBuilder itemBuilder;

    public TemplateItem(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public ItemBuilder getBuilder() {
        return itemBuilder.copy();
    }

    public ItemBuilder getEditableBuilder() {
        return itemBuilder;
    }

    public ItemStack build() {
        return getBuilder().build();
    }

    public ItemStack build(SuperiorPlayer superiorPlayer) {
        return getBuilder().build(superiorPlayer);
    }

    public TemplateItem copy() {
        return new TemplateItem(this.itemBuilder.copy());
    }

}
