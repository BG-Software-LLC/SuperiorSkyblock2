package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class MenuCoops extends PagedSuperiorMenu<SuperiorPlayer> {

    private final Island island;

    private MenuCoops(SuperiorPlayer superiorPlayer, Island island){
        super("menuCoops", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, SuperiorPlayer superiorPlayer) {
        try {
            return new ItemBuilder(clickedItem)
                    .replaceAll("{0}", superiorPlayer.getName())
                    .replaceAll("{1}", superiorPlayer.getPlayerRole() + "")
                    .asSkullOf(superiorPlayer).build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of player: " + superiorPlayer.getName());
            throw ex;
        }
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getCoopPlayers();
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> title
                .replace("{0}", String.valueOf(island.getCoopPlayers().size()))
                .replace("{1}", String.valueOf(island.getCoopLimit()))
        );
    }

    public static void init(){
        MenuCoops menuCoops = new MenuCoops(null, null);

        File file = new File(plugin.getDataFolder(), "menus/coops.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/coops.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuCoops, "coops.yml", cfg);

        menuCoops.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuCoops.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuCoops.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuCoops.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuCoops.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuCoops(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        SuperiorMenu.refreshMenus(MenuCoops.class, superiorMenu -> superiorMenu.island.equals(island));
    }

}
