package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandsTopMenu extends SuperiorMenu {


    private static IslandsTopMenu instance = null;
    private static Map<SortingType, Inventory> inventories = new HashMap<>();
    private static String title;

    private static Integer[] slots;
    private static int playerIslandSlot, worthSortSlot = -1, levelSortSlot = -1, ratingSortSlot = -1, playersSortSlot = -1;
    private static ItemStack noIslandItem, islandItem;

    private SortingType sortingType = SortingTypes.DEFAULT;

    private IslandsTopMenu(){
        super("islandTop");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(!clickSort(superiorPlayer, e.getRawSlot())){
            if(e.getRawSlot() == playerIslandSlot){
                clickItem(superiorPlayer.getIsland(), superiorPlayer, e.getAction());
                return;
            }

            else {
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] == e.getRawSlot()) {
                        Island island = plugin.getGrid().getIsland(i, sortingType);
                        if(clickItem(island, superiorPlayer, e.getAction()))
                            return;
                    }
                }
            }
        }

        super.onClick(e);
    }

    private boolean clickSort(SuperiorPlayer superiorPlayer, int slot){
        SortingType sortingType = null;

        if(slot == worthSortSlot){
            sortingType = SortingTypes.BY_WORTH;
        }
        else if(slot == levelSortSlot){
            sortingType = SortingTypes.BY_LEVEL;
        }
        else if(slot == ratingSortSlot){
            sortingType = SortingTypes.BY_RATING;
        }
        else if(slot == playersSortSlot){
            sortingType = SortingTypes.BY_PLAYERS;
        }

        if(sortingType != null){
            superiorPlayer.asPlayer().closeInventory();
            SoundWrapper sound = getSound(slot);
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            List<String> commands = getCommands(slot);
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
            open(superiorPlayer, null, sortingType);
        }

        return sortingType != null;
    }

    private boolean clickItem(Island island, SuperiorPlayer superiorPlayer, InventoryAction inventoryAction){
        if(island != null) {
            superiorPlayer.asPlayer().closeInventory();
            SoundWrapper sound = getSound(-1);
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            List<String> commands = getCommands(-1);
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
            if(inventoryAction == InventoryAction.PICKUP_HALF){
                IslandWarpsMenu.openInventory(superiorPlayer, this, island);
            } else {
                IslandValuesMenu.openInventory(superiorPlayer, this, island);
            }
            return true;
        }

        SoundWrapper sound = getSound(-2);
        if(sound != null)
            sound.playSound(superiorPlayer.asPlayer());
        List<String> commands = getCommands(-2);
        if(commands != null)
            commands.forEach(command ->
                    Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                            command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));

        return false;
    }

    @Override
    public Inventory getInventory() {
        ensureType(SortingTypes.DEFAULT);
        return inventories.get(SortingTypes.DEFAULT);
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, previousMenu, SortingTypes.DEFAULT);
    }

    private void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SortingType sortingType){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, previousMenu, sortingType));
            return;
        }

        reloadGUI(sortingType);

        ensureType(sortingType);

        Inventory inv = Bukkit.createInventory(this, inventories.get(sortingType).getSize(), title);
        reloadInventory(inv, superiorPlayer, sortingType);

        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    private void reloadInventory(Inventory inv, SuperiorPlayer superiorPlayer, SortingType sortingType){
        ensureType(sortingType);
        inv.setContents(inventories.get(sortingType).getContents());

        if(playerIslandSlot != -1){
            Island island = superiorPlayer.getIsland();
            int i = island == null ? -1 : plugin.getGrid().getIslandPosition(island, sortingType) + 1;
            inv.setItem(playerIslandSlot, getTopItem(island, i));
        }
    }

    private void reloadGUI(SortingType sortingType){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> reloadGUI(sortingType));
            return;
        }

        ensureType(sortingType);

        this.sortingType = sortingType;

        plugin.getGrid().sortIslands(sortingType);

        Inventory inventory = inventories.get(sortingType);

        for(int i = 0; i < slots.length; i++){
            Island island = i >= plugin.getGrid().getSize() ? null : plugin.getGrid().getIsland(i, sortingType);
            ItemStack itemStack = getTopItem(island, i + 1);
            inventory.setItem(slots[i], itemStack);
        }

        if(playerIslandSlot != -1)
            inventory.setItem(playerIslandSlot, getTopItem(null, -1));
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
            String islandName = !plugin.getSettings().islandNamesIslandTop || island.getName().isEmpty() ?
                    islandOwner.getName() : plugin.getSettings().islandNamesColorSupport ?
                    ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();

            itemBuilder.replaceName("{0}", islandName)
                    .replaceName("{1}", String.valueOf(place))
                    .replaceName("{2}", island.getIslandLevelAsBigDecimal().toString())
                    .replaceName("{3}", island.getWorthAsBigDecimal().toString())
                    .replaceName("{5}", StringUtils.fancyFormat(island.getIslandLevelAsBigDecimal()))
                    .replaceName("{6}", StringUtils.fancyFormat(island.getWorthAsBigDecimal()));

            if(itemStack.getItemMeta().hasLore()){
                List<String> lore = new ArrayList<>();

                for(String line : itemStack.getItemMeta().getLore()){
                    if(line.contains("{4}")){
                        List<SuperiorPlayer> members = island.getIslandMembers(plugin.getSettings().islandTopIncludeLeader);
                        String memberFormat = line.split("\\{4}:")[1];
                        if(members.size() == 0){
                            lore.add(memberFormat.replace("{}", "None"));
                        }
                        else {
                            members.forEach(member -> lore.add(memberFormat.replace("{}", member.getName())));
                        }
                    }else{
                        lore.add(line
                                .replace("{0}", island.getOwner().getName())
                                .replace("{1}", String.valueOf(place))
                                .replace("{2}", island.getIslandLevelAsBigDecimal().toString())
                                .replace("{3}", island.getWorthAsBigDecimal().toString())
                                .replace("{5}", StringUtils.fancyFormat(island.getIslandLevelAsBigDecimal()))
                                .replace("{6}", StringUtils.fancyFormat(island.getWorthAsBigDecimal())));
                    }
                }

                itemBuilder.withLore(lore);
            }
        }

        return itemBuilder.build();
    }

    private void ensureType(SortingType sortingType){
        if(!inventories.containsKey(sortingType))
            throw new IllegalStateException("The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

    public static void init(){
        IslandsTopMenu islandsTopMenu = new IslandsTopMenu();

        islandsTopMenu.resetData();
        inventories.clear();

        File file = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/top-islands.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(SortingType sortingType : SortingType.values()){
            Inventory inventory = FileUtils.loadGUI(islandsTopMenu, cfg.getConfigurationSection("top-islands"), 6, "&lTop Islands");

            if (cfg.contains("top-islands.worth-sort")) {
                worthSortSlot = cfg.getInt("top-islands.worth-sort.slot");
                inventory.setItem(worthSortSlot, FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.worth-sort")));
                islandsTopMenu.addSound(worthSortSlot, FileUtils.getSound(cfg.getConfigurationSection("top-islands.worth-sort.sound")));
                islandsTopMenu.addCommands(worthSortSlot, cfg.getStringList("top-islands.worth-sort.commands"));
            }

            if (cfg.contains("top-islands.level-sort")) {
                levelSortSlot = cfg.getInt("top-islands.level-sort.slot");
                inventory.setItem(levelSortSlot, FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.level-sort")));
                islandsTopMenu.addSound(levelSortSlot, FileUtils.getSound(cfg.getConfigurationSection("top-islands.level-sort.sound")));
                islandsTopMenu.addCommands(levelSortSlot, cfg.getStringList("top-islands.level-sort.commands"));
            }

            if (cfg.contains("top-islands.rating-sort")) {
                ratingSortSlot = cfg.getInt("top-islands.rating-sort.slot");
                inventory.setItem(ratingSortSlot, FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.rating-sort")));
                islandsTopMenu.addSound(ratingSortSlot, FileUtils.getSound(cfg.getConfigurationSection("top-islands.rating-sort.sound")));
                islandsTopMenu.addCommands(ratingSortSlot, cfg.getStringList("top-islands.rating-sort.commands"));
            }

            if (cfg.contains("top-islands.players-sort")) {
                playersSortSlot = cfg.getInt("top-islands.players-sort.slot");
                inventory.setItem(playersSortSlot, FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.players-sort")));
                islandsTopMenu.addSound(playersSortSlot, FileUtils.getSound(cfg.getConfigurationSection("top-islands.players-sort.sound")));
                islandsTopMenu.addCommands(playersSortSlot, cfg.getStringList("top-islands.players-sort.commands"));
            }

            inventories.put(sortingType, inventory);
        }

        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("top-islands.title"));

        ItemStack islandItem = FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.island-item"));
        ItemStack noIslandItem = FileUtils.getItemStack(cfg.getConfigurationSection("top-islands.no-island-item"));

        islandsTopMenu.addSound(-1, FileUtils.getSound(cfg.getConfigurationSection("top-islands.island-item.sound")));
        islandsTopMenu.addSound(-2, FileUtils.getSound(cfg.getConfigurationSection("top-islands.no-island-item.sound")));
        islandsTopMenu.addCommands(-1, cfg.getStringList("top-islands.island-item.commands"));
        islandsTopMenu.addCommands(-2, cfg.getStringList("top-islands.no-island-item.commands"));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("top-islands.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));

        IslandsTopMenu.islandItem = islandItem;
        IslandsTopMenu.noIslandItem = noIslandItem;
        IslandsTopMenu.slots = slots.toArray(new Integer[0]);
        IslandsTopMenu.playerIslandSlot = cfg.getInt("top-islands.player-island-slot", -1);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SortingType sortingType){
        instance.reloadGUI(sortingType);
        instance.open(superiorPlayer, previousMenu, sortingType);
    }

}
