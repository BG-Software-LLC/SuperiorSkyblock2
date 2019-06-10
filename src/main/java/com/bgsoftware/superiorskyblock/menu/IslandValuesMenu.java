package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.HeadUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class IslandValuesMenu extends SuperiorMenu {

    private static Inventory valuesPage;
    private static Sound blockSound;
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
        e.setCancelled(true);
    }

    @Override
    public void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        Inventory inventory = Bukkit.createInventory(this, valuesPage.getSize(),
                valuesPage.getTitle().replace("{0}", island.getOwner().getName())
                        .replace("{1}", island.getWorthAsBigDecimal().toString()));
        inventory.setContents(valuesPage.getContents());

        new SuperiorThread(() -> {
            for(Key key : countedBlocks.keySet()){
                String[] sections = key.toString().split(":");
                ItemStack itemStack = new ItemStack(Material.valueOf(sections[0]));
                int slot = countedBlocks.get(key);

                String typeName = StringUtil.format(sections[0]);
                int amount = island.getBlockCount(SKey.of(itemStack));

                if(sections.length == 2) {
                    if(itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                        EntityType entityType = EntityType.valueOf(sections[1]);
                        amount = island.getBlockCount(SKey.of(Materials.SPAWNER.toBukkitType() + ":" + entityType));
                        itemStack = HeadUtil.getEntityHead(entityType);
                        typeName = StringUtil.format(sections[1]) + " Spawner";
                    }
                    else {
                        itemStack.setDurability(Short.valueOf(sections[1]));
                        amount = island.getBlockCount(SKey.of(itemStack));
                    }
                }

                itemStack = new ItemBuilder(itemStack).withName(blockName).withLore(blockLore)
                        .replaceAll("{0}", typeName).replaceAll("{1}", String.valueOf(amount)).build();

                if(amount == 0)
                    amount = 1;
                else if(amount > 64)
                    amount = 64;

                itemStack.setAmount(amount);

                inventory.setItem(slot, itemStack);
            }

            if(openSound != null)
                superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);

            superiorPlayer.asPlayer().openInventory(inventory);

            if(previousMenu != null)
                previousMenus.put(superiorPlayer.getUniqueId(), previousMenu);
        }).start();
    }

    public static IslandValuesMenu createInventory(Island island){
        return new IslandValuesMenu(island);
    }

    public static void init(){
        IslandValuesMenu islandValuesMenu = new IslandValuesMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/values-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/values-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        valuesPage = FileUtil.loadGUI(islandValuesMenu, cfg.getConfigurationSection("values-gui"), 6, "{0} &n${1}");

        Sound blockSound = getSound(cfg.getString("values-gui.block-item.sound", ""));
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

        IslandValuesMenu.blockSound = blockSound;
        IslandValuesMenu.blockName = blockName;
        IslandValuesMenu.blockLore = blockLore;
        IslandValuesMenu.countedBlocks = countedBlocks;
    }

    private static Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

}
