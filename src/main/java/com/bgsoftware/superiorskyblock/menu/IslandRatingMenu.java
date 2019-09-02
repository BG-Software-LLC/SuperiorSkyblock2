package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IslandRatingMenu extends SuperiorMenu {

    private static IslandRatingMenu instance;
    private static Inventory inventory = null;
    private static int oneStarSlot, twoStarsSlot, threeStarsSlot, fourStarsSlot, fiveStarsSlot;

    private static Map<UUID, Island> islands = new HashMap<>();

    private IslandRatingMenu(){
        super("ratingPage");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Island island = islands.get(e.getWhoClicked().getUniqueId());
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

        island.setRating(e.getWhoClicked().getUniqueId(), rating);

        Locale.RATE_SUCCESS.send(e.getWhoClicked(), rating.getValue());

        e.getWhoClicked().closeInventory();
        super.onClick(e);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void open(SuperiorPlayer superiorPlayer, Island island, SuperiorMenu previousMenu){
        islands.put(superiorPlayer.getUniqueId(), island);
        super.open(superiorPlayer, previousMenu);
    }

    @Override
    public void closeInventory(SuperiorPlayer superiorPlayer) {
        super.closeInventory(superiorPlayer);
        islands.remove(superiorPlayer.getUniqueId());
    }

    public static void init(){
        IslandRatingMenu islandRatingMenu = new IslandRatingMenu();

        File file = new File(plugin.getDataFolder(), "guis/rating-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/rating-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandRatingMenu, cfg.getConfigurationSection("rating-gui"), InventoryType.HOPPER, "&l         Rate Island");

        ItemStack oneStarItem = FileUtil.getItemStack(cfg.getConfigurationSection("rating-gui.one_star"));
        oneStarSlot = cfg.getInt("rating-gui.one_star.slot", 0);
        islandRatingMenu.addSound(oneStarSlot, getSound(cfg.getConfigurationSection("rating-gui.one_star.sound")));
        islandRatingMenu.addCommands(oneStarSlot, cfg.getStringList("rating-gui.one_star.commands"));
        inventory.setItem(oneStarSlot, oneStarItem);

        ItemStack twoStarsItem = FileUtil.getItemStack(cfg.getConfigurationSection("rating-gui.two_stars"));
        twoStarsSlot = cfg.getInt("rating-gui.two_stars.slot", 0);
        islandRatingMenu.addSound(twoStarsSlot, getSound(cfg.getConfigurationSection("rating-gui.two_stars.sound")));
        islandRatingMenu.addCommands(twoStarsSlot, cfg.getStringList("rating-gui.two_stars.commands"));
        inventory.setItem(twoStarsSlot, twoStarsItem);

        ItemStack threeStarsItem = FileUtil.getItemStack(cfg.getConfigurationSection("rating-gui.three_stars"));
        threeStarsSlot = cfg.getInt("rating-gui.three_stars.slot", 0);
        islandRatingMenu.addSound(threeStarsSlot, getSound(cfg.getConfigurationSection("rating-gui.three_stars.sound")));
        islandRatingMenu.addCommands(threeStarsSlot, cfg.getStringList("rating-gui.three_stars.commands"));
        inventory.setItem(threeStarsSlot, threeStarsItem);

        ItemStack fourStarsItem = FileUtil.getItemStack(cfg.getConfigurationSection("rating-gui.four_stars"));
        fourStarsSlot = cfg.getInt("rating-gui.four_stars.slot", 0);
        islandRatingMenu.addSound(fourStarsSlot, getSound(cfg.getConfigurationSection("rating-gui.four_stars.sound")));
        islandRatingMenu.addCommands(fourStarsSlot, cfg.getStringList("rating-gui.four_stars.commands"));
        inventory.setItem(fourStarsSlot, fourStarsItem);

        ItemStack fiveStarsItem = FileUtil.getItemStack(cfg.getConfigurationSection("rating-gui.five_stars"));
        fiveStarsSlot = cfg.getInt("rating-gui.five_stars.slot", 0);
        islandRatingMenu.addSound(fiveStarsSlot, getSound(cfg.getConfigurationSection("rating-gui.five_stars.sound")));
        islandRatingMenu.addCommands(fiveStarsSlot, cfg.getStringList("rating-gui.five_stars.commands"));
        inventory.setItem(fiveStarsSlot, fiveStarsItem);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, Island island, SuperiorMenu previousMenu){
        instance.open(superiorPlayer, island, previousMenu);
    }

}
