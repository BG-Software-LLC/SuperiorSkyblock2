package com.bgsoftware.superiorskyblock.gui.menus.types.warps;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.gui.menus.types.panel.MemberMenu;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandWarpsMenu extends YamlScroll {

    private Island island;
    private ItemStack template;

    public IslandWarpsMenu(Player player, Island island) {
        super(player, MenuTemplate.ISLAND_WARPS.getFile());
        create(title, rows);

        this.island = island;
        template = ItemSerializer.getItem("BOOK", file.getConfigurationSection("warp-item"));

        setList(getButtonsList());

        setPage(0);
        open();
    }

    private List<Button> getButtonsList() {
        List<Button> buttons = new ArrayList<>();

        for (String warp : island.getAllWarps()) {
            ItemStack item = ItemSerializer.replace(template.clone(), warp, getLocation(island.getWarpLocation(warp)));
            buttons.add(new Button(item, (clicker, type) -> {
                clicker.closeInventory();
                clicker.teleport(island.getWarpLocation(warp));
            }));
        }

        return buttons;
    }

    private static String getLocation(Location location){
        return location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

}
