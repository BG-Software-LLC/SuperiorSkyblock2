package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class IslandValuesMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private static String blockName;
    private static List<String> blockLore;
    private static KeyMap<Integer> countedBlocks;

    private Island island;

    private IslandValuesMenu(Island island){
        super("valuesPage");
        this.island = island;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(
                this,
                inventory.getSize(),
                title.replace("{0}", island.getOwner().getName()).replace("{1}", island.getWorthAsBigDecimal().toString())
        );

        inv.setContents(inventory.getContents());

        for(Key key : countedBlocks.keySet()){
            String[] sections = key.toString().split(":");
            ItemStack itemStack = new ItemStack(Material.valueOf(sections[0]));
            int slot = countedBlocks.get(key);

            String typeName = StringUtils.format(sections[0]);
            int amount = island.getBlockCount(Key.of(itemStack));

            if(sections.length == 2) {
                if(itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                    EntityType entityType = EntityType.valueOf(sections[1]);
                    amount = island.getExactBlockCount(Key.of(Materials.SPAWNER.toBukkitType() + ":" + entityType));
                    itemStack = HeadUtils.getEntityHead(entityType);
                    typeName = StringUtils.format(sections[1]) + " Spawner";
                }
                else {
                    itemStack.setDurability(Short.parseShort(sections[1]));
                    amount = island.getBlockCount(Key.of(itemStack));
                }
            }

            itemStack = new ItemBuilder(itemStack).withName(blockName).withLore(blockLore)
                    .replaceAll("{0}", typeName).replaceAll("{1}", String.valueOf(amount)).build();

            if(amount == 0)
                amount = 1;
            else if(amount > 64)
                amount = 64;

            itemStack.setAmount(amount);

            inv.setItem(slot, itemStack);
        }

        return inv;
    }

    public static void init(){
        IslandValuesMenu islandValuesMenu = new IslandValuesMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/values-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/values-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandValuesMenu, cfg.getConfigurationSection("values-gui"), 6, "{0} &n${1}");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("values-gui.title"));

        String blockName = cfg.getString("values-gui.block-item.name", "&e&l[!] &7{0}");
        List<String> blockLore = cfg.getStringList("values-gui.block-item.lore");

        KeyMap<Integer> countedBlocks = new KeyMap<>();

        for(String materialName : cfg.getStringList("values-gui.materials")){
            String[] sections = materialName.split(":");
            if(sections.length == 2){
                countedBlocks.put(sections[0], Integer.valueOf(sections[1]));
            }else{
                countedBlocks.put(sections[0] + ":" + sections[1], Integer.valueOf(sections[2]));
            }
        }

        IslandValuesMenu.blockName = blockName;
        IslandValuesMenu.blockLore = blockLore;
        IslandValuesMenu.countedBlocks = countedBlocks;
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandValuesMenu(island).open(superiorPlayer, previousMenu);
    }

}
