package com.bgsoftware.superiorskyblock.gui.menus.types;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VisitorsMenu extends YamlScroll {

    private Island island;
    private ItemStack template;

    public VisitorsMenu(Player player, Island island) {
        super(player, MenuTemplate.VISITORS.getFile());
        create(title, rows);

        this.island = island;
        template = ItemSerializer.getItem("SKULL_ITEM:3", file.getConfigurationSection("visitor-item"));

        setList(getButtonsList());

        setPage(0);
        open();
    }

    private List<Button> getButtonsList() {
        List<Button> buttons = new ArrayList<>();

        for (UUID uuid : island.getVisitors()) {
            SuperiorPlayer member = SSuperiorPlayer.of(uuid);
            buttons.add(new PlayerButton(template, member, (clicker, type) -> {
                if (member == null || type != ClickType.RIGHT)
                    return;

                player.performCommand("island invite " + member.getName());
            }));
        }

        return buttons;
    }

}
