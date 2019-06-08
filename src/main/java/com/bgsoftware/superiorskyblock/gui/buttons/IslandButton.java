package com.bgsoftware.superiorskyblock.gui.buttons;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.BiConsumer;

public class IslandButton extends Button{

    public IslandButton(Island island, int rank, ItemStack template, BiConsumer<? super Player, ? super ClickType> action) {
        super(getIslandItem(island, rank, template), action);
    }

    private static ItemStack getIslandItem(Island island, int rank, ItemStack template) {
        ItemStack item = ItemSerializer.replace(
                template.clone(),
                island.getOwner().getName(),
                rank + "",
                island.getIslandLevelAsBigDecimal().toString(),
                island.getWorthAsBigDecimal().toString()
        );
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwner(island.getOwner().getName());
        item.setItemMeta(meta);

        return item;
    }
}
