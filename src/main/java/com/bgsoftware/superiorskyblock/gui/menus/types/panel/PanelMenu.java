package com.bgsoftware.superiorskyblock.gui.menus.types.panel;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.entity.Player;

public class PanelMenu extends YamlMenu {

    public PanelMenu(Player player) {
        super(player, MenuTemplate.PANEL.getFile());
        create(title, rows);

        Island island = SSuperiorPlayer.of(player).getIsland();

        addAction("members", (a, b) -> new MembersMenu(player, island));
        addAction("settings", (a, b) -> {});
        addAction("visitors", (a, b) -> new VisitorsMenu(player, island));

        load();
        open();
    }

}
