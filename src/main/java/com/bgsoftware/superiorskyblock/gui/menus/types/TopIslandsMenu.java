package com.bgsoftware.superiorskyblock.gui.menus.types;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.IslandButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class TopIslandsMenu extends YamlScroll {

    private ItemStack islandItem;
    private ItemStack invalidItem;

    public TopIslandsMenu(Player player) {
        super(player, MenuTemplate.TOP_ISLANDS.getFile());
        create(title, rows);

        islandItem = ItemSerializer.getItem("SKULL_ITEM:3", file.getConfigurationSection("island_item"));
        invalidItem = ItemSerializer.getItem("SKULL_ITEM:3", file.getConfigurationSection("invalid_item"));
        SkullMeta meta = (SkullMeta) invalidItem.getItemMeta();
        meta.setOwner("MHF_Question");
        invalidItem.setItemMeta(meta);

        setList(createButtons());

        setPage(0);
        open();
    }

    private List<Button> createButtons() {
        List<Button> buttons = new ArrayList<>();

        int rank = 1;
        for (Island island : SuperiorSkyblockPlugin.getPlugin().getGrid().getIslands()) {
            buttons.add(new IslandButton(island, rank, islandItem, (clicker, type) -> {}));
            rank++;
        }

        while (buttons.size() % getPageSize() != 0) {
            buttons.add(new Button(invalidItem, (c, t) -> {}));
        }

        return buttons;
    }

}
