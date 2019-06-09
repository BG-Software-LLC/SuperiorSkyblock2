package com.bgsoftware.superiorskyblock.gui.menus;

import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class YamlMenu extends Menu {

    protected YamlConfiguration file;

    protected Map<String, ItemStack> items;
    protected Map<String, BiConsumer<? super Player, ? super ClickType>> actions;

    public YamlMenu(Player player, YamlConfiguration file) {
        super(player, ChatColor.translateAlternateColorCodes('&', file.getString("title")), file.getInt("rows"));
        this.file = file;

        actions = new HashMap<>();
        actions.put("exit", this::exit);
    }

    public void load() {
        loadItems();
        loadButtons();

        update();
    }

    @SuppressWarnings("all")
    protected void loadItems() {
        items = new HashMap<>();

        if (!file.contains("items"))
            return;

        file.getConfigurationSection("items").getKeys(false).forEach(key -> items.put(key, ItemSerializer.getItem(file.getConfigurationSection("items." + key))));
    }

    protected void loadButtons() {
        if (!file.contains("buttons"))
            return;

        for (Map<?, ?> map : file.getMapList("buttons")) {
            loadButton(map);
        }
    }

    @SuppressWarnings("all")
    protected int loadButton(Map<?, ?> map) {
        String[] position = ((String) map.get("position")).replace(" ", "").split(",");
        int x = Integer.valueOf(position[0]);
        int y = Integer.valueOf(position[1]);

        ItemStack item = items.get(map.get("item"));
        BiConsumer<? super Player, ? super ClickType> action = actions.get(map.get("action"));

        Button button = new Button(item, action);

        if (map.containsKey("commands"))
            button.setCommands((List<String>) map.get("commands"));
        if (map.containsKey("console"))
            button.setConsole((Boolean) map.get("console"));

        setButton(x, y, button);

        return coordsToSlot(x, y);
    }

    public void addAction(String key, BiConsumer<? super Player, ? super ClickType> action) {
        actions.put(key, action);
    }

    private void exit(Player clicker, ClickType type) {
        player.closeInventory();
    }

    public Map<String, ItemStack> getItems() {
        return items;
    }
}
