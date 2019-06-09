package com.bgsoftware.superiorskyblock.gui.menus.types.warps;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.gui.menus.types.panel.MemberMenu;
import com.bgsoftware.superiorskyblock.gui.menus.types.statistics.TopIslandsMenu;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandWarpsMenu extends YamlScroll {

    private PreviousMenu previous;

    private Island island;
    private ItemStack template;

    public IslandWarpsMenu(Player player, Island island, PreviousMenu previous) {
        super(player, MenuTemplate.ISLAND_WARPS.getFile());
        create(title, rows);

        this.previous = previous;
        this.island = island;
        template = ItemSerializer.getItem("BOOK", file.getConfigurationSection("warp-item"));

        canExit = false;

        setList(getButtonsList());

        if (buttons.isEmpty())
            load();

        open();
    }

    private List<Button> getButtonsList() {
        List<Button> buttons = new ArrayList<>();

        for (String warp : island.getAllWarps()) {
            ItemStack item = ItemSerializer.replace(template.clone(), warp, getLocation(island.getWarpLocation(warp)));
            buttons.add(new Button(item, (clicker, type) -> {
                canExit = true;
                clicker.closeInventory();
                clicker.teleport(island.getWarpLocation(warp));
            }));
        }

        return buttons;
    }

    private static String getLocation(Location location){
        return location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    @Override
    public void onClose() {
        if (canExit)
            return;

        switch (previous) {
            case WARPS:
                new WarpsMenu(player);
                break;
            case TOP:
                new TopIslandsMenu(player);
                break;
        }
    }

    public enum PreviousMenu {
        NONE,
        WARPS,
        TOP
    }

}
