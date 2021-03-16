package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class MenuTopIslands extends PagedSuperiorMenu<Island> {

    private static List<Integer> playerIslandSlot;
    private static boolean sortGlowWhenSelected;

    private SortingType sortingType;
    private final Set<SortingType> alreadySorted = new HashSet<>();

    private MenuTopIslands(SuperiorPlayer superiorPlayer, SortingType sortingType){
        super("menuTopIslands", superiorPlayer, true);
        this.sortingType = sortingType;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Island island) {
        if(!clickSort(event.getRawSlot())){
            if(playerIslandSlot.contains(event.getRawSlot())){
                clickItem(superiorPlayer.getIsland(), superiorPlayer, event.getAction());
            }

            else {
                clickItem(island, superiorPlayer, event.getAction());
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, sortingType);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Island island) {
        SuperiorPlayer islandOwner = island == null ? null : island.getOwner();
        try {
            int place = island == null ? 0 : plugin.getGrid().getIslandPosition(island, sortingType) + 1;

            ItemBuilder itemBuilder = ((ItemBuilder) getData(islandOwner == null ? "no-island-item" : "island-item")).clone()
                    .asSkullOf(islandOwner);

            if (island != null && islandOwner != null) {
                String islandName = !plugin.getSettings().islandNamesIslandTop || island.getName().isEmpty() ?
                        islandOwner.getName() : plugin.getSettings().islandNamesColorSupport ?
                        StringUtils.translateColors(island.getName()) : island.getName();

                itemBuilder.replaceName("{0}", islandName)
                        .replaceName("{1}", String.valueOf(place))
                        .replaceName("{2}", StringUtils.format(island.getIslandLevel()))
                        .replaceName("{3}", StringUtils.format(island.getWorth()))
                        .replaceName("{5}", StringUtils.fancyFormat(island.getIslandLevel(), superiorPlayer.getUserLocale()))
                        .replaceName("{6}", StringUtils.fancyFormat(island.getWorth(), superiorPlayer.getUserLocale()))
                        .replaceName("{7}", StringUtils.format(island.getTotalRating()))
                        .replaceName("{8}", StringUtils.formatRating(Locale.getDefaultLocale(), island.getTotalRating()))
                        .replaceName("{9}", StringUtils.format(island.getRatingAmount()))
                        .replaceName("{10}", StringUtils.format(island.getAllPlayersInside().size()));

                if (itemBuilder.getItemMeta().hasLore()) {
                    List<String> lore = new ArrayList<>();

                    for (String line : itemBuilder.getItemMeta().getLore()) {
                        if (line.contains("{4}")) {
                            List<SuperiorPlayer> members = island.getIslandMembers(plugin.getSettings().islandTopIncludeLeader);
                            String memberFormat = line.split("\\{4}:")[1];
                            if (members.size() == 0) {
                                lore.add(memberFormat.replace("{}", "None"));
                            } else {
                                members.forEach(member -> {
                                    String onlineMessage = member.isOnline() ?
                                            Locale.ISLAND_TOP_STATUS_ONLINE.getMessage(superiorPlayer.getUserLocale()) :
                                            Locale.ISLAND_TOP_STATUS_OFFLINE.getMessage(superiorPlayer.getUserLocale());

                                    lore.add(PlaceholderHook.parse(member, memberFormat
                                            .replace("{}", member.getName())
                                            .replace("{0}", member.getName())
                                            .replace("{1}", onlineMessage == null ? "" : onlineMessage))
                                    );
                                });
                            }
                        } else {
                            lore.add(line
                                    .replace("{0}", island.getOwner().getName())
                                    .replace("{1}", String.valueOf(place))
                                    .replace("{2}", StringUtils.format(island.getIslandLevel()))
                                    .replace("{3}", StringUtils.format(island.getWorth()))
                                    .replace("{5}", StringUtils.fancyFormat(island.getIslandLevel(), superiorPlayer.getUserLocale()))
                                    .replace("{6}", StringUtils.fancyFormat(island.getWorth(), superiorPlayer.getUserLocale()))
                                    .replace("{7}", StringUtils.format(island.getTotalRating()))
                                    .replace("{8}", StringUtils.formatRating(Locale.getDefaultLocale(), island.getTotalRating()))
                                    .replace("{9}", StringUtils.format(island.getRatingAmount()))
                                    .replace("{10}", StringUtils.format(island.getAllPlayersInside().size())));
                        }
                    }

                    itemBuilder.withLore(lore);
                }

                return itemBuilder.build(islandOwner);
            }

            return itemBuilder.build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of the island of: " + (islandOwner == null ? "null" : islandOwner.getName()));
            throw ex;
        }
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
            //noinspection unchecked
            List<Integer> glowSlots = (List<Integer>) getData(sortingType.getName(), Collections.singletonList(-1));
            glowSlots.stream().filter(slot -> slot != -1).forEach(slot ->
                inventory.setItem(slot, new ItemBuilder(inventory.getItem(slot)).withEnchant(EnchantsUtils.getGlowEnchant(), 1).build(superiorPlayer)));
        }

        for(int playerIslandSlot : playerIslandSlot) {
            if (playerIslandSlot != -1) {
                Island island = superiorPlayer.getIsland();
                inventory.setItem(playerIslandSlot, getObjectItem(null, island));
            }
        }

        return inventory;
    }

    private boolean clickSort(int slot) {
        SortingType sortingType = null;

        try{
            sortingType = SortingType.getByName((String) getData(slot + "", ""));
        }catch(IllegalArgumentException ignored){ }

        if(sortingType != null){
            this.sortingType = sortingType;
            previousMove = false;

            if(alreadySorted.add(sortingType)){
                plugin.getGrid().sortIslands(sortingType, () -> open(previousMenu));
            }
            else {
                open(previousMenu);
            }
        }

        return sortingType != null;
    }

    private void clickItem(Island island, SuperiorPlayer superiorPlayer, InventoryAction inventoryAction){
        if(island != null) {
            SoundWrapper sound = (SoundWrapper) getData("island-sound");
            if(sound != null)
                superiorPlayer.runIfOnline(sound::playSound);
            //noinspection unchecked
            List<String> commands = (List<String>) getData("island-commands");
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));

            previousMove = false;

            if(inventoryAction == InventoryAction.PICKUP_HALF) {
                if(MenuGlobalWarps.visitorWarps){
                    CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "visit " + island.getOwner().getName());
                }
                else {
                    MenuWarpCategories.openInventory(superiorPlayer, this, island);
                }
            } else if(plugin.getSettings().valuesMenu) {
                MenuValues.openInventory(superiorPlayer, this, island);
            }

            return;
        }

        SoundWrapper sound = (SoundWrapper) getData("no-island-sound");
        if(sound != null)
            superiorPlayer.runIfOnline(sound::playSound);
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
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        sortGlowWhenSelected = cfg.getBoolean("sort-glow-when-selected", false);

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuTopIslands.resetData();

        menuTopIslands.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menuTopIslands.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menuTopIslands.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menuTopIslands.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

        List<String> pattern = cfg.getStringList("pattern");

        menuTopIslands.setRowsSize(pattern.size());
        int backButton = -1;
        char backButtonChar = cfg.getString("back", " ").charAt(0);

        Registry<Character, List<Integer>> charSlots = Registry.createRegistry();

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(backButtonChar == ch){
                        backButton = slot;
                    }

                    menuTopIslands.addFillItem(slot, FileUtils.getItemStack("top-islands.yml", cfg.getConfigurationSection("items." + ch)));
                    menuTopIslands.addCommands(slot, cfg.getStringList("commands." + ch));
                    menuTopIslands.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                    String permission = cfg.getString("permissions." + ch + ".permission");
                    SoundWrapper noAccessSound = FileUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));
                    menuTopIslands.addPermission(slot, permission, noAccessSound);

                    if(cfg.contains("items." + ch + ".sorting-type")) {
                        String sortingType = cfg.getString("items." + ch + ".sorting-type");
                        menuTopIslands.addData(slot + "", sortingType);
                        //noinspection unchecked
                        List<Integer> slots = (List<Integer>) menuTopIslands.getData(sortingType, new ArrayList<>());
                        slots.add(slot);
                        menuTopIslands.addData(sortingType, slots);
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.add(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        menuTopIslands.setBackButton(backButton);

        if(plugin.getSettings().onlyBackButton && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[top-islands.yml] Menu doesn't have a back button, it's impossible to close it.");

        if(cfg.contains("worth-sort")) {
            List<Integer> worthSortSlots = getSlots(cfg, "worth-sort", charSlots);
            worthSortSlots.forEach(slot ->  menuTopIslands.addData(slot + "", "WORTH"));
            menuTopIslands.addData("WORTH", worthSortSlots);
        }
        if(cfg.contains("level-sort")) {
            List<Integer> levelSortSlots = getSlots(cfg, "level-sort", charSlots);
            levelSortSlots.forEach(slot ->  menuTopIslands.addData(slot + "", "LEVEL"));
            menuTopIslands.addData("LEVEL", levelSortSlots);
        }
        if(cfg.contains("rating-sort")) {
            List<Integer> ratingSortSlots = getSlots(cfg, "rating-sort", charSlots);
            ratingSortSlots.forEach(slot ->  menuTopIslands.addData(slot + "", "RATING"));
            menuTopIslands.addData("RATING", ratingSortSlots);
        }
        if(cfg.contains("players-sort")) {
            List<Integer> playerSortSlots = getSlots(cfg, "players-sort", charSlots);
            playerSortSlots.forEach(slot ->  menuTopIslands.addData(slot + "", "PLAYERS"));
            menuTopIslands.addData("PLAYERS", playerSortSlots);
        }

        playerIslandSlot = getSlots(cfg, "player-island", charSlots);

        char slotsChar = cfg.getString("slots", " ").charAt(0);

        menuTopIslands.addData("island-item", FileUtils.getItemStack("top-islands.yml", cfg.getConfigurationSection("items." + slotsChar + ".island")));
        menuTopIslands.addData("no-island-item", FileUtils.getItemStack("top-islands.yml", cfg.getConfigurationSection("items." + slotsChar + ".no-island")));
        menuTopIslands.addData("island-sound", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".island")));
        menuTopIslands.addData("no-island-sound", FileUtils.getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".no-island")));
        menuTopIslands.addData("island-commands", cfg.getStringList("commands." + slotsChar + ".island"));
        menuTopIslands.addData("no-island-commands", cfg.getStringList("commands." + slotsChar + ".no-island"));

        menuTopIslands.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuTopIslands.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuTopIslands.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuTopIslands.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuTopIslands.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SortingType sortingType){
        plugin.getGrid().sortIslands(sortingType, () -> new MenuTopIslands(superiorPlayer, sortingType).open(previousMenu));
    }

    public static void refreshMenus(SortingType sortingType){
        SuperiorMenu.refreshMenus(MenuTopIslands.class, superiorMenu -> superiorMenu.sortingType.equals(sortingType));
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
