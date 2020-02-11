package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(titleReplacer);

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

            if(inventoryAction == InventoryAction.PICKUP_HALF) {
                MenuWarps.openInventory(superiorPlayer, this, island);
            } else if(plugin.getSettings().valuesMenu) {
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

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

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

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("top-islands.title"));

        int size = cfg.getInt("top-islands.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("top-islands.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("top-islands.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++], worthChar = itemChars[charCounter++],
                levelChar = itemChars[charCounter++], ratingChar = itemChars[charCounter++],
                playersChar = itemChars[charCounter++], playerIslandChar = itemChars[charCounter++];

        for(String slot : cfg.getString("top-islands.slots").split(","))
            patternChars[Integer.parseInt(slot)] = slotsChar;

        ConfigurationSection islandItemSection = cfg.getConfigurationSection("top-islands.island-item");
        newMenu.set("items."+ slotsChar + ".island", islandItemSection);
        newMenu.set("sounds."+ slotsChar + ".island", islandItemSection.getConfigurationSection("sound"));
        islandItemSection.set("sound", null);

        ConfigurationSection noIslandItemSection = cfg.getConfigurationSection("top-islands.no-island-item");
        newMenu.set("items."+ slotsChar + ".no-island", noIslandItemSection);
        newMenu.set("sounds."+ slotsChar + ".no-island", noIslandItemSection.getConfigurationSection("sound"));
        noIslandItemSection.set("sound", null);

        if(cfg.contains("top-islands.worth-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.worth-sort"), patternChars, worthChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if(cfg.contains("top-islands.level-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.level-sort"), patternChars, levelChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if(cfg.contains("top-islands.rating-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.rating-sort"), patternChars, ratingChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if(cfg.contains("top-islands.players-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.players-sort"), patternChars, playersChar,
                    itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("player-island-slot"))
            patternChars[cfg.getInt("player-island-slot")] = playerIslandChar;

        newMenu.set("worth-sort", worthChar);
        newMenu.set("level-sort", levelChar);
        newMenu.set("rating-sort", ratingChar);
        newMenu.set("players-sort", playersChar);
        newMenu.set("player-island", playerIslandChar);
        newMenu.set("sort-glow-when-selected", false);

        char invalidChar = itemChars[charCounter++];

        newMenu.set("slots", slotsChar);
        newMenu.set("previous-page", invalidChar);
        newMenu.set("current-page", invalidChar);
        newMenu.set("next-page", invalidChar);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
