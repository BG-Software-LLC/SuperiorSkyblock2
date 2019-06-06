package com.bgsoftware.superiorskyblock.gui.buttons;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class Button {

    protected ItemStack item;

    protected BiConsumer<? super Player, ? super ClickType> action;

    public Button(ItemStack item, BiConsumer<? super Player, ? super ClickType> action) {
        this.item = item;
        this.action = action;
    }

    public void setItem(ItemStack item) {
        this.item.setType(item.getType());
        this.item.setData(item.getData());
        this.item.setAmount(item.getAmount());
        this.item.setItemMeta(item.getItemMeta());
    }

    public ItemStack getItem() {
        return item;
    }

    public BiConsumer<? super Player, ? super ClickType> getAction() {
        return action;
    }
}
