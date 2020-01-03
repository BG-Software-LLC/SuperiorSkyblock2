package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MenuIslandRatings extends PagedMappedSuperiorMenu<UUID, Rating> {

    private Island island;

    private MenuIslandRatings(SuperiorPlayer superiorPlayer, Island island){
        super("menuIslandRatings", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, UUID clickedObjectKey, Rating clickedObjectValue) {

    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, UUID uuid, Rating rating) {
        SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(uuid);
        return new ItemBuilder(clickedItem)
                .replaceAll("{0}", _superiorPlayer.getName())
                .replaceAll("{1}", StringUtils.formatRating(_superiorPlayer.getUserLocale(), rating.getValue()))
                .asSkullOf(_superiorPlayer).build(superiorPlayer);
    }

    @Override
    protected Map<UUID, Rating> requestMappedObjects() {
        return island.getRatings();
    }

    public static void init(){
        MenuIslandRatings menuIslandRatings = new MenuIslandRatings(null, null);

        File file = new File(plugin.getDataFolder(), "menus/island-ratings.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-ratings.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandRatings, "island-ratings.yml", cfg);

        menuIslandRatings.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandRatings.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandRatings.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuIslandRatings.setSlots(charSlots.getOrDefault(cfg.getString("warps", "@").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuIslandRatings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuIslandRatings.class);
    }

}
