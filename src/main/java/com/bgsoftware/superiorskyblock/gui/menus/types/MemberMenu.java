package com.bgsoftware.superiorskyblock.gui.menus.types;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberMenu extends YamlMenu {

    private SuperiorPlayer member;

    public MemberMenu(Player player, SuperiorPlayer member) {
        super(player, MenuTemplate.MEMBER.getFile());
        create(title.replace("{0}", member.getName()), rows);

        this.member = member;

        addAction("role", this::role);
        addAction("kick", this::kick);
        addAction("ban", this::ban);

        load();
        open();
    }

    private void role(Player clicker, ClickType type) {

    }

    private void kick(Player clicker, ClickType type) {
        clicker.performCommand("is kick " + member.getName());
    }

    private void ban(Player clicker, ClickType type) {
        clicker.performCommand("is ban " + member.getName());
    }

}
