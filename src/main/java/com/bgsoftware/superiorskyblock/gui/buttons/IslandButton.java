package com.bgsoftware.superiorskyblock.gui.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

        List<String> lore = meta.getLore();
        List<String> newLore = new ArrayList<>();

        for (String line : lore) {
            if (!line.contains("{MEMBER}")) {
                newLore.add(line);
                continue;
            }

            for (UUID uuid : island.getAllMembers()) {
                if (!SuperiorSkyblockPlugin.getPlugin().getSettings().islandTopIncludeLeader && uuid.equals(island.getOwner().getUniqueId()))
                    continue;
                newLore.add(line.replace("{MEMBER}", SSuperiorPlayer.of(uuid).getName()));
            }
        }
        meta.setLore(newLore);

        meta.setOwner(island.getOwner().getName());
        item.setItemMeta(meta);

        return item;
    }
}
