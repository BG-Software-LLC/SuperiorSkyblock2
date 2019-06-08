package com.bgsoftware.superiorskyblock.gui.menus.types.islands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.menus.YamlMenu;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

public class IslandBiomeMenu extends YamlMenu {

    public IslandBiomeMenu(Player player) {
        super(player, MenuTemplate.ISLAND_BIOME.getFile());
        create(title, rows);

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

        final String permission = map.containsKey("permission") ? (String) map.get("permission") : null;

        item = ItemSerializer.replace(item, (permission == null || player.hasPermission(permission) ? ChatColor.GREEN + "(Available)" : ChatColor.RED + "(Unavailable)"));

        if (map.containsKey("biome")) {
            action = (clicker, type) -> {
                if (permission == null || player.hasPermission(permission)) {
                    SuperiorPlayer sPlayer = SSuperiorPlayer.of(clicker);
                    if (sPlayer.getIsland() == null)
                        return;
                    sPlayer.getIsland().setBiome(Biome.valueOf((String) map.get("biome")));
                    Locale.CHANGED_BIOME.send(sPlayer, map.get("biome"));
                }
            };
        }

        setButton(x, y, new Button(item, action));

        return coordsToSlot(x, y);
    }

}
