package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public final class MenuWarpManage extends SuperiorMenu {

    private static List<Integer> renameSlots, iconSlots, locationSlots, privateSlots;
    private static SoundWrapper successUpdateSound;

    private final IslandWarp islandWarp;

    private MenuWarpManage(SuperiorPlayer superiorPlayer, IslandWarp islandWarp){
        super("menuWarpManage", superiorPlayer);
        this.islandWarp = islandWarp;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        if(renameSlots.contains(e.getRawSlot())){
            previousMove = false;
            e.getWhoClicked().closeInventory();

            Locale.WARP_RENAME.send(e.getWhoClicked());

            PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                if(!message.equalsIgnoreCase("-cancel")) {
                    String newName = IslandUtils.getWarpName(message);

                    if (islandWarp.getIsland().getWarp(newName) != null) {
                        Locale.WARP_RENAME_ALREADY_EXIST.send(e.getWhoClicked());
                        return true;
                    }

                    islandWarp.getIsland().renameWarp(islandWarp, newName);

                    Locale.WARP_RENAME_SUCCESS.send(e.getWhoClicked(), newName);

                    if (successUpdateSound != null)
                        successUpdateSound.playSound(e.getWhoClicked());
                }

                open(previousMenu);
                PlayerChat.remove((Player) e.getWhoClicked());

                return true;
            });
        }

        else if(iconSlots.contains(e.getRawSlot())){
            previousMove = false;
            MenuWarpIconEdit.openInventory(superiorPlayer, this, islandWarp);
        }

        else if(locationSlots.contains(e.getRawSlot())){
            if(!islandWarp.getIsland().isInsideRange(superiorPlayer.getLocation())){
                Locale.SET_WARP_OUTSIDE.send(superiorPlayer);
                return;
            }

            Locale.WARP_LOCATION_UPDATE.send(e.getWhoClicked());

            Block signBlock = islandWarp.getLocation().getBlock();
            if(signBlock.getState() instanceof Sign){
                signBlock.setType(Material.AIR);
                signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
                Locale.DELETE_WARP_SIGN_BROKE.send(superiorPlayer);
            }

            islandWarp.setLocation(e.getWhoClicked().getLocation());

            if(successUpdateSound != null)
                successUpdateSound.playSound(e.getWhoClicked());
        }

        else if(privateSlots.contains(e.getRawSlot())){
            islandWarp.setPrivateFlag(!islandWarp.hasPrivateFlag());
            if(islandWarp.hasPrivateFlag())
                Locale.WARP_PRIVATE_UPDATE.send(e.getWhoClicked());
            else
                Locale.WARP_PUBLIC_UPDATE.send(e.getWhoClicked());
        }

    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, islandWarp);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(title -> title.replace("{0}", islandWarp.getName()));

        iconSlots.forEach(slot -> {
            ItemBuilder itemBuilder = islandWarp.getRawIcon() == null ?
                    SIslandWarp.DEFAULT_WARP_ICON.clone() : new ItemBuilder(islandWarp.getRawIcon());

            ItemStack currentItem = inventory.getItem(slot);

            if(currentItem != null && currentItem.hasItemMeta()) {
                ItemMeta itemMeta = currentItem.getItemMeta();
                if(itemMeta.hasDisplayName())
                    itemBuilder.withName(itemMeta.getDisplayName());

                if(itemMeta.hasLore())
                    itemBuilder.appendLore(itemMeta.getLore());
            }

            inventory.setItem(slot, itemBuilder.build(islandWarp.getIsland().getOwner()));
        });

        return inventory;
    }

    public static void init(){
        MenuWarpManage menuWarpManage = new MenuWarpManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warp-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warp-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarpManage, "warp-manage.yml", cfg);

        renameSlots = getSlots(cfg, "warp-rename", charSlots);
        iconSlots = getSlots(cfg, "warp-icon", charSlots);
        locationSlots = getSlots(cfg, "warp-location", charSlots);
        privateSlots = getSlots(cfg, "warp-private", charSlots);

        if(cfg.isConfigurationSection("success-update-sound"))
            successUpdateSound = FileUtils.getSound(cfg.getConfigurationSection("success-update-sound"));

        charSlots.delete();

        menuWarpManage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, IslandWarp islandWarp){
        new MenuWarpManage(superiorPlayer, islandWarp).open(previousMenu);
    }

    public static void refreshMenus(IslandWarp islandWarp){
        refreshMenus(MenuWarpManage.class, superiorMenu -> superiorMenu.islandWarp.equals(islandWarp));
    }

}
