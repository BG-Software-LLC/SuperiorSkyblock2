package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class MenuValues extends SuperiorMenu {

    private Island island;

    private MenuValues(SuperiorPlayer superiorPlayer, Island island){
        super("menuValues", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.buildInventory(title -> title.replace("{0}", island.getOwner().getName()).replace("{1}", island.getWorth().toString()));

        for(int slot = 0; slot < inventory.getSize(); slot++){
            if(containsData(slot + "")){
                Key block = (Key) getData(slot + "");
                int amount = island.getBlockCount(block);
                ItemStack itemStack = new ItemBuilder(inventory.getItem(slot)).replaceAll("{0}", amount + "").build(superiorPlayer);
                itemStack.setAmount(Math.max(1, Math.min(64, itemStack.getAmount())));
                inventory.setItem(slot, itemStack);
            }
        }

        return inventory;
    }

    public static void init(){
        MenuValues menuValues = new MenuValues(null, null);

        File file = new File(plugin.getDataFolder(), "menus/values.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/values.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuValues.resetData();

        menuValues.setTitle(ChatColor.translateAlternateColorCodes('&', cfg.getString("title", "")));
        menuValues.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));

        List<String> pattern = cfg.getStringList("pattern");

        menuValues.setRowsSize(pattern.size());

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(cfg.contains("items." + ch + ".block"))
                        menuValues.addData(slot + "", Key.of(cfg.getString("items." + ch + ".block")));

                    menuValues.addFillItem(slot,  FileUtils.getItemStack(cfg.getConfigurationSection("items." + ch)));
                    menuValues.addCommands(slot, cfg.getStringList("commands." + ch));
                    menuValues.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                    slot++;
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuValues(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuValues.class);
    }

}
