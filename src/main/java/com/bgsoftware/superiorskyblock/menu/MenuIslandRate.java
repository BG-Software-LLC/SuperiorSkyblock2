package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MenuIslandRate extends SuperiorMenu {

    private static List<Integer> zeroStarsSlot, oneStarSlot, twoStarsSlot, threeStarsSlot, fourStarsSlot, fiveStarsSlot;

    private final Island island;

    private MenuIslandRate(SuperiorPlayer superiorPlayer, Island island){
        super("menuIslandRate", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Rating rating = Rating.UNKNOWN;

        if(zeroStarsSlot.contains(e.getRawSlot()))
            rating = Rating.ZERO_STARS;
        else if(oneStarSlot.contains(e.getRawSlot()))
            rating = Rating.ONE_STAR;
        else if(twoStarsSlot.contains(e.getRawSlot()))
            rating = Rating.TWO_STARS;
        else if(threeStarsSlot.contains(e.getRawSlot()))
            rating = Rating.THREE_STARS;
        else if(fourStarsSlot.contains(e.getRawSlot()))
            rating = Rating.FOUR_STARS;
        else if(fiveStarsSlot.contains(e.getRawSlot()))
            rating = Rating.FIVE_STARS;

        if(rating == Rating.UNKNOWN)
            return;

        island.setRating(superiorPlayer, rating);

        Locale.RATE_SUCCESS.send(e.getWhoClicked(), rating.getValue());

        IslandUtils.sendMessage(island, Locale.RATE_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), rating.getValue());

        Executor.sync(() -> {
            previousMove = false;
            e.getWhoClicked().closeInventory();
        }, 1L);
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, island, previousMenu);
    }

    public static void init(){
        MenuIslandRate menuIslandRate = new MenuIslandRate(null, null);

        File file = new File(plugin.getDataFolder(), "menus/island-rate.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-rate.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandRate, "island-rate.yml", cfg);

        zeroStarsSlot = getSlots(cfg, "zero-stars", charSlots);
        oneStarSlot = getSlots(cfg, "one-star", charSlots);
        twoStarsSlot = getSlots(cfg, "two-stars", charSlots);
        threeStarsSlot = getSlots(cfg, "three-stars", charSlots);
        fourStarsSlot = getSlots(cfg, "four-stars", charSlots);
        fiveStarsSlot = getSlots(cfg, "five-stars", charSlots);

        charSlots.delete();

        menuIslandRate.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, Island island, SuperiorMenu previousMenu){
        new MenuIslandRate(superiorPlayer, island).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/ratings-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("rate-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("rate-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("rate-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char oneStarChar = itemChars[charCounter++], twoStarsChar = itemChars[charCounter++],
                threeStarsChar = itemChars[charCounter++], fourStarsChar = itemChars[charCounter++],
                fiveStarsChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.one_star"), patternChars, oneStarChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.two_stars"), patternChars, twoStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.three_stars"), patternChars, threeStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.four_stars"), patternChars, fourStarsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("rate-gui.five_stars"), patternChars, fiveStarsChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("one-star", oneStarChar + "");
        newMenu.set("two-stars", twoStarsChar + "");
        newMenu.set("three-stars", threeStarsChar + "");
        newMenu.set("four-stars", fourStarsChar + "");
        newMenu.set("five-stars", fiveStarsChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars, itemChars[charCounter]));

        return true;
    }

}
