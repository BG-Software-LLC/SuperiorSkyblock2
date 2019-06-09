package com.bgsoftware.superiorskyblock.gui.menus.types.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.HeadUtil;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WarpsMenu extends YamlScroll {

    private ItemStack template;

    public WarpsMenu(Player player) {
        super(player, MenuTemplate.WARPS.getFile());
        create(title, rows);

        template = ItemSerializer.getItem(HeadUtil.getMaterial(), file.getConfigurationSection("island-item"));

        setList(getButtonsList());

        if (buttons.isEmpty())
            load();

        open();
    }

    private List<Button> getButtonsList() {
        List<Button> buttons = new ArrayList<>();

        for (Island island : SuperiorSkyblockPlugin.getPlugin().getGrid().getIslands()) {
            if (island.getAllWarps().isEmpty())
                continue;

            buttons.add(new PlayerButton(template, island.getOwner(), (clicker, type) -> new IslandWarpsMenu(player, island, IslandWarpsMenu.PreviousMenu.WARPS)));
        }

        return buttons;
    }

}
