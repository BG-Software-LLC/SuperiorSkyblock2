package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class IslandRatingsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";
    private static ItemStack previousButton, currentButton, nextButton, rateItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private Map<UUID, Rating> ratings;
    private int page;

    private IslandRatingsMenu(Island island){
        super("ratingsPage");
        if(island != null)
            this.ratings = island.getRatings();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            int nextPage;

            if(clickedSlot == previousSlot){
                nextPage = page == 1 ? -1 : page - 1;
            }
            else if(clickedSlot == nextSlot){
                nextPage = ratings.size() > page * slots.size() ? page + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            open(superiorPlayer, nextPage, previousMenu);
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        this.page = page;

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        Iterator<UUID> uuids = ratings.keySet().iterator();

        int currentIndex = 0;

        while(uuids.hasNext() && currentIndex < slots.size() && (currentIndex + (slots.size() * (page - 1))) < ratings.size()){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(uuids.next());

            StringBuilder starsString = new StringBuilder();
            int rating = ratings.get(_superiorPlayer.getUniqueId()).getValue();
            if(rating >= 1)
                starsString.append(Locale.ISLAND_INFO_RATE_ONE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 2)
                starsString.append(Locale.ISLAND_INFO_RATE_TWO_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 3)
                starsString.append(Locale.ISLAND_INFO_RATE_THREE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 4)
                starsString.append(Locale.ISLAND_INFO_RATE_FOUR_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 5)
                starsString.append(Locale.ISLAND_INFO_RATE_FIVE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            for(int i = 5; i > rating; i--)
                starsString.append(Locale.ISLAND_INFO_RATE_EMPTY_SYMBOL.getMessage());

            inv.setItem(slots.get(currentIndex), new ItemBuilder(rateItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .replaceAll("{1}", starsString.toString())
                    .asSkullOf(_superiorPlayer).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceAll("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceAll("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceAll("{0}", (ratings.size() > page * slots.size() ? "&a" : "&c")).build());


        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    public static void init(){
        IslandRatingsMenu islandRatingsMenu = new IslandRatingsMenu(null);

        File file = new File(plugin.getDataFolder(), "guis/ratings-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/ratings-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandRatingsMenu, cfg.getConfigurationSection("ratings-gui"), 6, "&lIsland Ratings");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("ratings-gui.title"));

        previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("ratings-gui.previous-page"));
        currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("ratings-gui.current-page"));
        nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("ratings-gui.next-page"));
        rateItem = FileUtil.getItemStack(cfg.getConfigurationSection("ratings-gui.rate-item"));

        previousSlot = cfg.getInt("ratings-gui.previous-page.slot");
        currentSlot = cfg.getInt("ratings-gui.current-page.slot");
        nextSlot = cfg.getInt("ratings-gui.next-page.slot");

        islandRatingsMenu.addSound(previousSlot, getSound(cfg.getConfigurationSection("ratings-gui.previous-page.sound")));
        islandRatingsMenu.addSound(currentSlot, getSound(cfg.getConfigurationSection("ratings-gui.current-page.sound")));
        islandRatingsMenu.addSound(nextSlot, getSound(cfg.getConfigurationSection("ratings-gui.next-page.sound")));
        islandRatingsMenu.addSound(-1, getSound(cfg.getConfigurationSection("ratings-gui.rate-item.sound")));
        islandRatingsMenu.addCommands(previousSlot, cfg.getStringList("ratings-gui.previous-page.commands"));
        islandRatingsMenu.addCommands(currentSlot, cfg.getStringList("ratings-gui.current-page.commands"));
        islandRatingsMenu.addCommands(nextSlot, cfg.getStringList("ratings-gui.next-page.commands"));
        islandRatingsMenu.addCommands(-1, cfg.getStringList("ratings-gui.rate-item.commands"));

        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        Arrays.stream(cfg.getString("ratings-gui.rate-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandRatingsMenu(island).open(superiorPlayer, previousMenu);
    }

}
