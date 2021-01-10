package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public final class MenuWarpCategoryManage extends SuperiorMenu {

    private static List<Integer> renameSlots, iconSlots, warpsSlots;
    private static SoundWrapper successUpdateSound;

    private final WarpCategory warpCategory;

    private MenuWarpCategoryManage(SuperiorPlayer superiorPlayer, WarpCategory warpCategory){
        super("menuWarpCategoryManage", superiorPlayer);
        this.warpCategory = warpCategory;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        if(renameSlots.contains(e.getRawSlot())){
            previousMove = false;
            e.getWhoClicked().closeInventory();

            Locale.WARP_CATEGORY_RENAME.send(e.getWhoClicked());

            PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                if(!message.equalsIgnoreCase("-cancel")) {
                    String newName = IslandUtils.getWarpName(message);

                    if(warpCategory.getIsland().getWarpCategory(newName) != null){
                        Locale.WARP_CATEGORY_RENAME_ALREADY_EXIST.send(e.getWhoClicked());
                        return true;
                    }

                    warpCategory.getIsland().renameCategory(warpCategory, newName);

                    Locale.WARP_CATEGORY_RENAME_SUCCESS.send(e.getWhoClicked(), newName);

                    if(successUpdateSound != null)
                        successUpdateSound.playSound(e.getWhoClicked());
                }

                open(previousMenu);
                PlayerChat.remove((Player) e.getWhoClicked());

                return true;
            });
        }
        else if(iconSlots.contains(e.getRawSlot())){
            if(e.getClick().name().contains("RIGHT")){
                previousMove = false;
                MenuWarpCategoryIconEdit.openInventory(superiorPlayer, this, warpCategory);
            }
            else{
                previousMove = false;
                e.getWhoClicked().closeInventory();

                Locale.WARP_CATEGORY_SLOT.send(e.getWhoClicked());

                PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                    if(!message.equalsIgnoreCase("-cancel")) {
                        int slot;
                        try {
                            slot = Integer.parseInt(message);
                            if (slot < 0 || slot >= MenuWarpCategories.rowsSize * 9)
                                throw new IllegalArgumentException();
                        } catch (IllegalArgumentException ex) {
                            Locale.INVALID_SLOT.send(e.getWhoClicked(), message);
                            return true;
                        }

                        if (warpCategory.getIsland().getWarpCategory(slot) != null) {
                            Locale.WARP_CATEGORY_SLOT_ALREADY_TAKEN.send(e.getWhoClicked());
                            return true;
                        }

                        warpCategory.setSlot(slot);
                        Locale.WARP_CATEGORY_SLOT_SUCCESS.send(e.getWhoClicked(), slot);

                        if(successUpdateSound != null)
                            successUpdateSound.playSound(e.getWhoClicked());
                    }

                    open(previousMenu);
                    PlayerChat.remove((Player) e.getWhoClicked());

                    return true;
                });
            }
        }
        else if(warpsSlots.contains(e.getRawSlot())){
            previousMove = false;
            MenuWarps.openInventory(superiorPlayer, this, warpCategory);
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, warpCategory);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(title -> title.replace("{0}", warpCategory.getName()));

        iconSlots.forEach(slot -> {
            ItemBuilder itemBuilder = new ItemBuilder(warpCategory.getRawIcon());
            ItemStack currentItem = inventory.getItem(slot);

            if(currentItem != null && currentItem.hasItemMeta()) {
                ItemMeta itemMeta = currentItem.getItemMeta();
                if(itemMeta.hasDisplayName())
                    itemBuilder.withName(itemMeta.getDisplayName());

                if(itemMeta.hasLore())
                    itemBuilder.appendLore(itemMeta.getLore());
            }

            inventory.setItem(slot, itemBuilder.build(warpCategory.getIsland().getOwner()));
        });

        return inventory;
    }

    public static void init(){
        MenuWarpCategoryManage menuWarpCategoryManage = new MenuWarpCategoryManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warp-category-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warp-category-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarpCategoryManage, "warp-category-manage.yml", cfg);

        renameSlots = getSlots(cfg, "category-rename", charSlots);
        iconSlots = getSlots(cfg, "category-icon", charSlots);
        warpsSlots = getSlots(cfg, "category-warps", charSlots);

        if(cfg.isConfigurationSection("success-update-sound"))
            successUpdateSound = FileUtils.getSound(cfg.getConfigurationSection("success-update-sound"));

        charSlots.delete();

        menuWarpCategoryManage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, WarpCategory warpCategory){
        new MenuWarpCategoryManage(superiorPlayer, warpCategory).open(previousMenu);
    }

    public static void refreshMenus(WarpCategory warpCategory){
        refreshMenus(MenuWarpCategoryManage.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

}
