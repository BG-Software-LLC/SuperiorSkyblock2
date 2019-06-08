package com.bgsoftware.superiorskyblock.gui.menus.types.panel;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class MemberRoleMenu extends YamlMenu {

    private SuperiorPlayer member;

    public MemberRoleMenu(Player player, SuperiorPlayer member) {
        super(player, MenuTemplate.MEMBER_ROLE.getFile());
        create(title.replace("{0}", member.getName()), rows);

        this.member = member;

        addAction("member", this::member);
        addAction("moderator", this::moderator);
        addAction("admin", this::admin);
        addAction("leader", this::leader);

        load();
        open();
    }

    private void member(Player clicker, ClickType type) {
        clicker.performCommand("is setrole " + member.getName() + " member");
    }

    private void moderator(Player clicker, ClickType type) {
        clicker.performCommand("is setrole " + member.getName() + " moderator");
    }

    private void admin(Player clicker, ClickType type) {
        clicker.performCommand("is setrole " + member.getName() + " admin");
    }

    private void leader(Player clicker, ClickType type) {
        clicker.performCommand("is transfer " + member.getName());
    }
}
