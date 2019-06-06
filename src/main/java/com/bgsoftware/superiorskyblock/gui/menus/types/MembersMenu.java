package com.bgsoftware.superiorskyblock.gui.menus.types;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.buttons.PlayerButton;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MembersMenu extends YamlScroll {

    private Island island;
    private ItemStack template;

    public MembersMenu(Player player, Island island) {
        super(player, MenuTemplate.MEMBERS.getFile());

        this.island = island;
        template = ItemSerializer.getItem("SKULL_ITEM:3", file.getConfigurationSection("member-item"));

        setList(getButtonsList());

        load();
        open();
    }

    private List<Button> getButtonsList() {
        List<Button> buttons = new ArrayList<>();

        for (UUID uuid : island.getAllMembers()) {
            SuperiorPlayer member = SSuperiorPlayer.of(uuid);
            buttons.add(new PlayerButton(template, member, (clicker, type) -> {}));
        }

        return buttons;
    }

}
