package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuIslandRate extends SuperiorMenu {

    private static int oneStarSlot, twoStarsSlot, threeStarsSlot, fourStarsSlot, fiveStarsSlot;

    private final Island island;

    private MenuIslandRate(SuperiorPlayer superiorPlayer, Island island){
        super("menuIslandRate", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Rating rating = Rating.UNKNOWN;

        if(e.getRawSlot() == oneStarSlot)
            rating = Rating.ONE_STAR;
        else if(e.getRawSlot() == twoStarsSlot)
            rating = Rating.TWO_STARS;
        else if(e.getRawSlot() == threeStarsSlot)
            rating = Rating.THREE_STARS;
        else if(e.getRawSlot() == fourStarsSlot)
            rating = Rating.FOUR_STARS;
        else if(e.getRawSlot() == fiveStarsSlot)
            rating = Rating.FIVE_STARS;

        if(rating == Rating.UNKNOWN)
            return;

        island.setRating(superiorPlayer, rating);

        Locale.RATE_SUCCESS.send(e.getWhoClicked(), rating.getValue());

        if(!Locale.RATE_ANNOUNCEMENT.isEmpty())
            island.sendMessage(Locale.RATE_ANNOUNCEMENT.getMessage(superiorPlayer.getName(), rating.getValue()));

        previousMove = false;
        e.getWhoClicked().closeInventory();
    }

    public static void init(){
        MenuIslandRate menuIslandRate = new MenuIslandRate(null, null);

        File file = new File(plugin.getDataFolder(), "menus/island-rate.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-rate.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandRate, "island-rate.yml", cfg);

        oneStarSlot = charSlots.getOrDefault(cfg.getString("one-star", "@").charAt(0), Collections.singletonList(-1)).get(0);
        twoStarsSlot = charSlots.getOrDefault(cfg.getString("two-stars", "%").charAt(0), Collections.singletonList(-1)).get(0);
        threeStarsSlot = charSlots.getOrDefault(cfg.getString("three-stars", "*").charAt(0), Collections.singletonList(-1)).get(0);
        fourStarsSlot = charSlots.getOrDefault(cfg.getString("four-stars", "^").charAt(0), Collections.singletonList(-1)).get(0);
        fiveStarsSlot = charSlots.getOrDefault(cfg.getString("five-stars", "&").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, Island island, SuperiorMenu previousMenu){
        new MenuIslandRate(superiorPlayer, island).open(previousMenu);
    }

}
