package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.IslandRegistry;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class IslandsTopMenu extends SuperiorMenu {

    private static IslandsTopMenu instance = null;
    private static Inventory inventory = null;

    private static Integer[] slots;
    private static ItemStack noIslandItem, islandItem;

    private IslandsTopMenu(){
        super("islandTop");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        for(int i = 0; i < slots.length; i++){
            if(slots[i] == e.getRawSlot()){
                Island island = plugin.getGrid().getIsland(i);

                if(island != null) {
                    superiorPlayer.asPlayer().closeInventory();
                    if(e.getAction() == InventoryAction.PICKUP_HALF){
                        IslandWarpsMenu.openInventory(superiorPlayer, this, island);
                    } else {
                        IslandValuesMenu.openInventory(superiorPlayer, this, island);
                    }
                    break;
                }

            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void reloadGUI(){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(this::reloadGUI).start();
            return;
        }

        IslandRegistry islands = plugin.getGrid().getIslandRegistry();
        islands.sort();

        for(int i = 0; i < slots.length; i++){
            Island island = i >= islands.size() ? null : islands.get(i);
            ItemStack itemStack = getTopItem(island, i + 1);
            inventory.setItem(slots[i], itemStack);
        }
    }

    private ItemStack getTopItem(Island island, int place){
        SuperiorPlayer islandOwner = island == null ? null : island.getOwner();

        ItemStack itemStack;

        if(islandOwner == null){
            itemStack = noIslandItem.clone();
        }

        else{
            itemStack = islandItem.clone();
        }

        ItemBuilder itemBuilder = new ItemBuilder(itemStack).asSkullOf(islandOwner);

        if(island != null && islandOwner != null) {
            itemBuilder.replaceName("{0}", islandOwner.getName())
                    .replaceName("{1}", String.valueOf(place))
                    .replaceName("{2}", island.getIslandLevelAsBigDecimal().toString())
                    .replaceName("{3}", island.getWorthAsBigDecimal().toString());

            if(itemStack.getItemMeta().hasLore()){
                List<String> lore = new ArrayList<>();

                for(String line : itemStack.getItemMeta().getLore()){
                    if(line.contains("{4}")){
                        String memberFormat = line.split("\\{4}:")[1];
                        if(island.getMembers().size() == 0){
                            lore.add(memberFormat.replace("{}", "None"));
                        }
                        else {
                            for (UUID memberUUID : plugin.getSettings().islandTopIncludeLeader ? island.getAllMembers() : island.getMembers()) {
                                lore.add(memberFormat.replace("{}", SSuperiorPlayer.of(memberUUID).getName()));
                            }
                        }
                    }else{
                        lore.add(line
                                .replace("{0}", island.getOwner().getName())
                                .replace("{1}", String.valueOf(place))
                                .replace("{2}", island.getIslandLevelAsBigDecimal().toString())
                                .replace("{3}", island.getWorthAsBigDecimal().toString()));
                    }
                }

                itemBuilder.withLore(lore);
            }
        }

        return itemBuilder.build();
    }

    public static void init(){
        IslandsTopMenu islandsTopMenu = new IslandsTopMenu();

        File file = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/top-islands.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandsTopMenu, cfg.getConfigurationSection("top-islands"), 6, "&lTop Islands");

        ItemStack islandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.island-item"));
        ItemStack noIslandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.no-island-item"));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("top-islands.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));

        IslandsTopMenu.islandItem = islandItem;
        IslandsTopMenu.noIslandItem = noIslandItem;
        IslandsTopMenu.slots = slots.toArray(new Integer[0]);

        islandsTopMenu.reloadGUI();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        instance.reloadGUI();
        instance.open(superiorPlayer, previousMenu);
    }

}
