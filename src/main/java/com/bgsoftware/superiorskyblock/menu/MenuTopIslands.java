package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuTopIslands extends PagedSuperiorMenu<Island> {

    private static int playerIslandSlot, worthSortSlot = -1, levelSortSlot = -1, ratingSortSlot = -1, playersSortSlot = -1;
    private static boolean sortGlowWhenSelected;

    private SortingType sortingType;

    private MenuTopIslands(SuperiorPlayer superiorPlayer, SortingType sortingType){
        super("menuTopIslands", superiorPlayer, true);
        this.sortingType = sortingType;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Island island) {
        if(!clickSort(event.getRawSlot())){
            if(event.getRawSlot() == playerIslandSlot){
                clickItem(superiorPlayer.getIsland(), superiorPlayer, event.getAction());
            }

            else {
                clickItem(island, superiorPlayer, event.getAction());
            }
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Island island) {
        SuperiorPlayer islandOwner = island == null ? null : island.getOwner();
        int place = island == null ? 0 : plugin.getGrid().getIslandPosition(island, sortingType) + 1;

        ItemBuilder itemBuilder = ((ItemBuilder) getData(islandOwner == null ? "no-island-item" : "island-item")).clone()
                .asSkullOf(islandOwner);

        if(island != null && islandOwner != null) {
            String islandName = !plugin.getSettings().islandNamesIslandTop || island.getName().isEmpty() ?
                    islandOwner.getName() : plugin.getSettings().islandNamesColorSupport ?
                    ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();

            itemBuilder.replaceName("{0}", islandName)
                    .replaceName("{1}", String.valueOf(place))
                    .replaceName("{2}", island.getIslandLevel().toString())
                    .replaceName("{3}", island.getWorth().toString())
                    .replaceName("{5}", StringUtils.fancyFormat(island.getIslandLevel()))
                    .replaceName("{6}", StringUtils.fancyFormat(island.getWorth()));

            if(itemBuilder.getItemMeta().hasLore()){
                List<String> lore = new ArrayList<>();

                for(String line : itemBuilder.getItemMeta().getLore()){
                    if(line.contains("{4}")){
                        List<SuperiorPlayer> members = island.getIslandMembers(plugin.getSettings().islandTopIncludeLeader);
                        String memberFormat = line.split("\\{4}:")[1];
                        if(members.size() == 0){
                            lore.add(memberFormat.replace("{}", "None"));
                        }
                        else {
                            members.forEach(member -> {
                                String onlineMessage = member.isOnline() ?
                                        Locale.ISLAND_TOP_STATUS_ONLINE.getMessage(superiorPlayer.getUserLocale()) :
                                        Locale.ISLAND_TOP_STATUS_OFFLINE.getMessage(superiorPlayer.getUserLocale());

                                lore.add(memberFormat
                                        .replace("{}", member.getName())
                                        .replace("{0}", member.getName())
                                        .replace("{1}", onlineMessage == null ? "" : onlineMessage)
                                );
                            });
                        }
                    }else{
                        lore.add(line
                                .replace("{0}", island.getOwner().getName())
                                .replace("{1}", String.valueOf(place))
                                .replace("{2}", island.getIslandLevel().toString())
                                .replace("{3}", island.getWorth().toString())
                                .replace("{5}", StringUtils.fancyFormat(island.getIslandLevel()))
                                .replace("{6}", StringUtils.fancyFormat(island.getWorth())));
                    }
                }

                itemBuilder.withLore(lore);
            }
        }

        return itemBuilder.build(superiorPlayer);
    }

    @Override
    protected ItemStack getNullItem() {
        return getObjectItem(null, null);
    }

    @Override
    protected List<Island> requestObjects() {
        return plugin.getGrid().getIslands(sortingType);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        if(sortGlowWhenSelected){
            int glowSlot = -1;

            if(sortingType == SortingTypes.BY_WORTH){
                glowSlot = worthSortSlot;
            }
            else if(sortingType == SortingTypes.BY_LEVEL){
                glowSlot = levelSortSlot;
            }
            else if(sortingType == SortingTypes.BY_RATING){
                glowSlot = ratingSortSlot;
            }
            else if(sortingType == SortingTypes.BY_PLAYERS){
                glowSlot = playersSortSlot;
            }

            if(glowSlot != -1){
                inventory.setItem(glowSlot, new ItemBuilder(inventory.getItem(glowSlot)).withEnchant(EnchantsUtils.getGlowEnchant(), 1).build(superiorPlayer));
            }
        }

        if(playerIslandSlot != -1){
            Island island = superiorPlayer.getIsland();
            inventory.setItem(playerIslandSlot, getObjectItem(null, island));
        }

        return inventory;
    }

    private boolean clickSort(int slot){
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
            this.sortingType = sortingType;
            previousMove = false;
            open(previousMenu);
        }

        return sortingType != null;
    }

    private void clickItem(Island island, SuperiorPlayer superiorPlayer, InventoryAction inventoryAction){
        if(island != null) {
            SoundWrapper sound = (SoundWrapper) getData("island-sound");
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            //noinspection unchecked
            List<String> commands = (List<String>) getData("island-commands");
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));

            previousMove = false;

            if(inventoryAction == InventoryAction.PICKUP_HALF){
                MenuWarps.openInventory(superiorPlayer, this, island);
            } else {
                MenuValues.openInventory(superiorPlayer, this, island);
            }

            return;
        }

        SoundWrapper sound = (SoundWrapper) getData("no-island-sound");
        if(sound != null)
            sound.playSound(superiorPlayer.asPlayer());
        //noinspection unchecked
        List<String> commands = (List<String>) getData("no-island-commands");
        if(commands != null)
            commands.forEach(command ->
                    Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                            command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
    }

    public static void init(){
        MenuTopIslands menuTopIslands = new MenuTopIslands(null, null);

        File file = new File(plugin.getDataFolder(), "menus/top-islands.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/top-islands.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        sortGlowWhenSelected = cfg.getBoolean("sort-glow-when-selected", false);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuTopIslands, "top-islands.yml", cfg);

        worthSortSlot = charSlots.getOrDefault(cfg.getString("worth-sort", "%").charAt(0), Collections.singletonList(-1)).get(0);
        levelSortSlot = charSlots.getOrDefault(cfg.getString("level-sort", "*").charAt(0), Collections.singletonList(-1)).get(0);
        ratingSortSlot = charSlots.getOrDefault(cfg.getString("rating-sort", "^").charAt(0), Collections.singletonList(-1)).get(0);
        playersSortSlot = charSlots.getOrDefault(cfg.getString("players-sort", "&").charAt(0), Collections.singletonList(-1)).get(0);
        playerIslandSlot = charSlots.getOrDefault(cfg.getString("player-island", "$").charAt(0), Collections.singletonList(-1)).get(0);

        char slotsChar = cfg.getString("slots", "@").charAt(0);

        menuTopIslands.addData("island-item", FileUtils.getItemStack("top-islands.yml", cfg.getConfigurationSection("items." + slotsChar + ".island")));
        menuTopIslands.addData("no-island-item", FileUtils.getItemStack("top-islands.yml", cfg.getConfigurationSection("items." + slotsChar + ".no-island")));
        menuTopIslands.addData("island-sound", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".island")));
        menuTopIslands.addData("no-island-sound", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".no-island")));
        menuTopIslands.addData("island-commands", cfg.getStringList("commands." + slotsChar + ".island"));
        menuTopIslands.addData("no-island-commands", cfg.getStringList("commands." + slotsChar + ".no-island"));

        menuTopIslands.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuTopIslands.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuTopIslands.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuTopIslands.setSlots(charSlots.getOrDefault(slotsChar, Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SortingType sortingType){
        new MenuTopIslands(superiorPlayer, sortingType).open(previousMenu);
        plugin.getGrid().sortIslands(sortingType);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuTopIslands.class);
    }

}
