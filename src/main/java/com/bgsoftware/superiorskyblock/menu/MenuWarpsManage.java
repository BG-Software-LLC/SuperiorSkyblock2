package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class MenuWarpsManage extends PagedSuperiorMenu<IslandWarp> {

    public static int rowsSize;
    private static List<String> editLore;

    private final WarpCategory warpCategory;

    private MenuWarpsManage(SuperiorPlayer superiorPlayer, WarpCategory warpCategory){
        super("menuWarpsManage", superiorPlayer);
        this.warpCategory = warpCategory;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, IslandWarp islandWarp) {
        previousMove = false;
        MenuWarpManage.openInventory(superiorPlayer, this, islandWarp);
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, warpCategory);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandWarp islandWarp) {
        try {
            ItemBuilder itemBuilder = new ItemBuilder(islandWarp.getRawIcon() == null ? clickedItem : islandWarp.getIcon(superiorPlayer));

            if(!editLore.isEmpty())
                itemBuilder.appendLore(editLore);

            return itemBuilder.replaceAll("{0}", islandWarp.getName())
                    .replaceAll("{1}", SBlockPosition.of(islandWarp.getLocation()).toString())
                    .replaceAll("{2}", islandWarp.hasPrivateFlag() ?
                            ensureNotNull(Locale.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                            ensureNotNull(Locale.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                    .build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of warp: " + islandWarp.getName());
            throw ex;
        }
    }

    @Override
    protected List<IslandWarp> requestObjects() {
        return warpCategory.getWarps();
    }

    public static void init(){
        MenuWarpsManage menuWarpsManage = new MenuWarpsManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warps-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warps-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarpsManage, "warps-manage.yml", cfg);

        menuWarpsManage.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuWarpsManage.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuWarpsManage.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuWarpsManage.setSlots(getSlots(cfg, "slots", charSlots));

        rowsSize = menuWarpsManage.getRowsSize();
        editLore = cfg.getStringList("edit-lore");

        charSlots.delete();

        menuWarpsManage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, WarpCategory warpCategory){
        MenuWarpsManage menuWarpsManage = new MenuWarpsManage(superiorPlayer, warpCategory);
        if(plugin.getSettings().skipOneItemMenus && hasOnlyOneItem(warpCategory, superiorPlayer)){
            menuWarpsManage.onPlayerClick(null, getOnlyOneItem(warpCategory, superiorPlayer));
        }
        else {
            menuWarpsManage.open(previousMenu);
        }
    }

    public static void refreshMenus(WarpCategory warpCategory){
        refreshMenus(MenuWarpsManage.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

    private String ensureNotNull(String check){
        return check == null ? "" : check;
    }

    private static boolean hasOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer){
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .count() == 1;
    }

    private static IslandWarp getOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer){
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .findFirst().orElse(null);
    }

}
