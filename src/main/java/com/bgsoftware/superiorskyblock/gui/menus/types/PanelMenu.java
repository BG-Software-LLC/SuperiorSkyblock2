package com.bgsoftware.superiorskyblock.gui.menus.types;

import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import org.bukkit.entity.Player;

public class PanelMenu extends YamlMenu {

    public PanelMenu(Player player) {
        super(player, MenuTemplate.PANEL.getFile());

        addAction("members", (a, b) -> {});
        addAction("settings", (a, b) -> {});
        addAction("visitors", (a, b) -> {});

        load();
        open();
    }

}
