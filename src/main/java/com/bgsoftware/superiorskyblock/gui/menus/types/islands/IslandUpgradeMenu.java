package com.bgsoftware.superiorskyblock.gui.menus.types.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

public class IslandUpgradeMenu extends YamlMenu {

    private Island island;

    public IslandUpgradeMenu(Player player) {
        super(player, MenuTemplate.ISLAND_UPGRADES.getFile());
        create(title, rows);

        this.island = SSuperiorPlayer.of(player).getIsland();

        addAction("hoppers-limit", this::hoppers);
        addAction("crop-growth", this::crops);
        addAction("spawner-rates", this::spawners);
        addAction("mob-drops", this::drops);
        addAction("members-limit", this::members);

        load();
        open();
    }

    @Override
    protected int loadButton(Map<?, ?> map) {
        String[] position = ((String) map.get("position")).replace(" ", "").split(",");
        int x = Integer.valueOf(position[0]);
        int y = Integer.valueOf(position[1]);

        ItemStack item = items.get(map.get("item"));
        BiConsumer<? super Player, ? super ClickType> action = actions.get(map.get("action"));

        if (map.containsKey("action")) {
            String name = (String) map.get("action");
            int level = island.getUpgradeLevel(name);
            String available = SuperiorSkyblockPlugin.getPlugin().getUpgrades().getMaxUpgradeLevel(name) <= level ? ChatColor.RED + "(Maxed)" : ChatColor.GREEN + "(Available)";
            String data = "data";
            double price = SuperiorSkyblockPlugin.getPlugin().getUpgrades().getUpgradePrice(name, level);
            item = ItemSerializer.replace(item, available, (level + 1) + "", data, price + "");
        }

        setButton(x, y, new Button(item, action));

        return coordsToSlot(x, y);
    }

    private void hoppers(Player clicker, ClickType type) {
        clicker.performCommand("is rankup hoppers-limit");
    }

    private void crops(Player clicker, ClickType type) {
        clicker.performCommand("is rankup crop-growth");
    }

    private void spawners(Player clicker, ClickType type) {
        clicker.performCommand("is rankup spawner-rates");
    }

    private void drops(Player clicker, ClickType type) {
        clicker.performCommand("is rankup mob-drops");
    }

    private void members(Player clicker, ClickType type) {
        clicker.performCommand("is rankup members-limit");
    }

}
