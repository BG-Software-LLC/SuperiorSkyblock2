package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IslandRateMenu extends SuperiorMenu {

    private static IslandRateMenu instance;
    private static Inventory inventory = null;
    private static int oneStarSlot, twoStarsSlot, threeStarsSlot, fourStarsSlot, fiveStarsSlot;

    private static Map<UUID, Island> islands = new HashMap<>();

    private IslandRateMenu(){
        super("ratePage");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Island island = islands.get(e.getWhoClicked().getUniqueId());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
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
        IslandRateMenu islandRatingMenu = new IslandRateMenu();

        File file = new File(plugin.getDataFolder(), "guis/ratings-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/ratings-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandRatingMenu, cfg.getConfigurationSection("rate-gui"), InventoryType.HOPPER, "&l         Rate Island");

        ItemStack oneStarItem = FileUtils.getItemStack(cfg.getConfigurationSection("rate-gui.one_star"));
        oneStarSlot = cfg.getInt("rate-gui.one_star.slot", 0);
        islandRatingMenu.addSound(oneStarSlot, FileUtils.getSound(cfg.getConfigurationSection("rate-gui.one_star.sound")));
        islandRatingMenu.addCommands(oneStarSlot, cfg.getStringList("rate-gui.one_star.commands"));
        inventory.setItem(oneStarSlot, oneStarItem);

        ItemStack twoStarsItem = FileUtils.getItemStack(cfg.getConfigurationSection("rate-gui.two_stars"));
        twoStarsSlot = cfg.getInt("rate-gui.two_stars.slot", 0);
        islandRatingMenu.addSound(twoStarsSlot, FileUtils.getSound(cfg.getConfigurationSection("rate-gui.two_stars.sound")));
        islandRatingMenu.addCommands(twoStarsSlot, cfg.getStringList("rate-gui.two_stars.commands"));
        inventory.setItem(twoStarsSlot, twoStarsItem);

        ItemStack threeStarsItem = FileUtils.getItemStack(cfg.getConfigurationSection("rate-gui.three_stars"));
        threeStarsSlot = cfg.getInt("rate-gui.three_stars.slot", 0);
        islandRatingMenu.addSound(threeStarsSlot, FileUtils.getSound(cfg.getConfigurationSection("rate-gui.three_stars.sound")));
        islandRatingMenu.addCommands(threeStarsSlot, cfg.getStringList("rate-gui.three_stars.commands"));
        inventory.setItem(threeStarsSlot, threeStarsItem);

        ItemStack fourStarsItem = FileUtils.getItemStack(cfg.getConfigurationSection("rate-gui.four_stars"));
        fourStarsSlot = cfg.getInt("rate-gui.four_stars.slot", 0);
        islandRatingMenu.addSound(fourStarsSlot, FileUtils.getSound(cfg.getConfigurationSection("rate-gui.four_stars.sound")));
        islandRatingMenu.addCommands(fourStarsSlot, cfg.getStringList("rate-gui.four_stars.commands"));
        inventory.setItem(fourStarsSlot, fourStarsItem);

        ItemStack fiveStarsItem = FileUtils.getItemStack(cfg.getConfigurationSection("rate-gui.five_stars"));
        fiveStarsSlot = cfg.getInt("rate-gui.five_stars.slot", 0);
        islandRatingMenu.addSound(fiveStarsSlot, FileUtils.getSound(cfg.getConfigurationSection("rate-gui.five_stars.sound")));
        islandRatingMenu.addCommands(fiveStarsSlot, cfg.getStringList("rate-gui.five_stars.commands"));
        inventory.setItem(fiveStarsSlot, fiveStarsItem);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, Island island, SuperiorMenu previousMenu){
        instance.open(superiorPlayer, island, previousMenu);
    }

}
