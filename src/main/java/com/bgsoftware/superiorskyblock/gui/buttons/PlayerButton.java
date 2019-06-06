package com.bgsoftware.superiorskyblock.gui.buttons;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.BiConsumer;

public class PlayerButton extends Button {

    private SuperiorPlayer player;

    public PlayerButton(ItemStack template, SuperiorPlayer player, BiConsumer<? super Player, ? super ClickType> action) {
        super(getItem(template, player), action);

        this.player = player;
    }

    private static ItemStack getItem(ItemStack template, SuperiorPlayer player) {
        ItemStack item = ItemSerializer.replace(template.clone(), player.getName());
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(player.getName());
        item.setItemMeta(meta);

        return item;
    }

}
