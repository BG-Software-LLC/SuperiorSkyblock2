package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public final class MenuWarpCategories extends SuperiorMenu {

    public static int rowsSize;
    private static List<String> editLore;

    private final Island island;
    private final boolean hasManagePerms;

    private MenuWarpCategories(SuperiorPlayer superiorPlayer, Island island){
        super("menuWarpCategories", superiorPlayer);
        this.island = island;
        hasManagePerms = island != null && island.hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        for(WarpCategory warpCategory : island.getWarpCategories().values()){
            if(e.getRawSlot() == warpCategory.getSlot()){
                if(e.getClick().name().contains("RIGHT") && hasManagePerms){
                    previousMove = false;
                    MenuWarpCategoryManage.openInventory(superiorPlayer, this, warpCategory);
                }
                else {
                    previousMove = false;
                    MenuWarps.openInventory(superiorPlayer, this, warpCategory);
                }
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(titleReplacer);

        for(WarpCategory warpCategory : island.getWarpCategories().values()) {
            boolean isMember = island.isMember(superiorPlayer);
            long accessAmount = warpCategory.getWarps().stream().filter(
                    islandWarp -> isMember || !islandWarp.hasPrivateFlag()
            ).count();

            if(accessAmount == 0)
                continue;

            ItemStack iconItem;

            if(!hasManagePerms || editLore.isEmpty()){
                iconItem = warpCategory.getIcon(island.getOwner());
            }
            else {
                iconItem = new ItemBuilder(warpCategory.getIcon(null))
                        .appendLore(editLore)
                        .build(island.getOwner());
            }

            inventory.setItem(warpCategory.getSlot(), iconItem);
        }

        return inventory;
    }

    public static void init(){
        MenuWarpCategories menuWarpCategories = new MenuWarpCategories(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warp-categories.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warp-categories.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarpCategories, "warp-categories.yml", cfg);

        charSlots.delete();

        rowsSize = menuWarpCategories.getRowsSize();
        editLore = cfg.getStringList("edit-lore");

        menuWarpCategories.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        MenuWarpCategories menuWarpCategories = new MenuWarpCategories(superiorPlayer, island);
        if(hasOnlyOneItem(island)){
            MenuWarps.openInventory(superiorPlayer, previousMenu, getOnlyOneItem(island));
        }
        else {
            menuWarpCategories.open(previousMenu);
        }
    }

    public static void refreshMenus(Island island){
        refreshMenus(MenuWarpCategories.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void destroyMenus(Island island){
        destroyMenus(MenuWarpCategories.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean hasOnlyOneItem(Island island){
        return island.getWarpCategories().size() <= 1;
    }

    private static WarpCategory getOnlyOneItem(Island island){
        return island.getWarpCategories().values().stream().findFirst().orElseGet(() -> island.createWarpCategory("Default Category"));
    }

}
