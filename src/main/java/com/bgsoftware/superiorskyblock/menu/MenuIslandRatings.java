package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MenuIslandRatings extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private Map<UUID, Rating> ratings;
    private int currentPage = 1;

    private MenuIslandRatings(SuperiorPlayer superiorPlayer, Island island){
        super("menuIslandRatings", superiorPlayer);
        if(island != null)
            this.ratings = island.getRatings();
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            if(clickedSlot == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < slots.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        Iterator<UUID> uuids = ratings.keySet().iterator();

        int currentIndex = 0;

        while(currentIndex < slots.size()){
            int ratingsIndex = currentIndex + (slots.size() * (currentPage - 1));

            if(ratingsIndex < ratings.size() && uuids.hasNext()) {
                SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(uuids.next());
                inventory.setItem(slots.get(currentIndex), new ItemBuilder(inventory.getItem(slots.get(currentIndex)))
                        .replaceAll("{0}", _superiorPlayer.getName())
                        .replaceAll("{1}", StringUtils.formatRating(_superiorPlayer.getUserLocale(), ratings.get(_superiorPlayer.getUniqueId()).getValue()))
                        .asSkullOf(_superiorPlayer).build(superiorPlayer));
            }
            else{
                inventory.setItem(slots.get(currentIndex), new ItemStack(Material.AIR));
            }

            currentIndex++;
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (ratings.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    public static void init(){
        MenuIslandRatings menuIslandRatings = new MenuIslandRatings(null, null);

        File file = new File(plugin.getDataFolder(), "menus/island-ratings.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-ratings.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandRatings, "island-ratings.yml", cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("warps", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuIslandRatings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuIslandRatings.class);
    }

}
