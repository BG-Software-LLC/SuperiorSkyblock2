package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MenuWarpIconEdit extends SuperiorMenu {

    private static List<Integer> typeSlots, renameSlots, loreSlots, confirmSlots, iconSlots;

    private final IslandWarp islandWarp;
    private final ItemStack itemStack;
    private String itemName = null;
    private List<String> itemLore = null;

    private MenuWarpIconEdit(SuperiorPlayer superiorPlayer, IslandWarp islandWarp){
        super("menuWarpIconEdit", superiorPlayer);
        this.islandWarp = islandWarp;
        this.itemStack = islandWarp == null ? null : islandWarp.getRawIcon() == null ?
                SIslandWarp.DEFAULT_WARP_ICON.clone().build() : islandWarp.getRawIcon();
        if(itemStack != null){
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemName = itemMeta.getDisplayName();
            itemLore = itemMeta.getLore();
        }
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        if(typeSlots.contains(e.getRawSlot())){
            previousMove = false;
            e.getWhoClicked().closeInventory();

            Locale.WARP_ICON_NEW_TYPE.send(e.getWhoClicked());

            PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                if(!message.equalsIgnoreCase("-cancel")) {
                    String[] sections = message.split(":");
                    Material material;

                    try {
                        material = Material.valueOf(sections[0].toUpperCase());
                        if (material == Material.AIR)
                            throw new IllegalArgumentException();
                    } catch (IllegalArgumentException ex) {
                        Locale.INVALID_MATERIAL.send(e.getWhoClicked(), message);
                        return true;
                    }

                    String rawMessage = sections.length == 2 ? sections[1] : "0";

                    short data;

                    try {
                        data = Short.parseShort(rawMessage);
                        if (data < 0)
                            throw new IllegalArgumentException();
                    } catch (IllegalArgumentException ex) {
                        Locale.INVALID_MATERIAL_DATA.send(e.getWhoClicked(), rawMessage);
                        return true;
                    }

                    itemStack.setType(material);
                    itemStack.setDurability(data);
                }

                open(previousMenu);
                PlayerChat.remove((Player) e.getWhoClicked());

                return true;
            });
        }
        else if(renameSlots.contains(e.getRawSlot())){
            previousMove = false;
            e.getWhoClicked().closeInventory();

            Locale.WARP_ICON_NEW_NAME.send(e.getWhoClicked());

            PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                if(!message.equalsIgnoreCase("-cancel")) {
                    itemName = message;
                }

                open(previousMenu);
                PlayerChat.remove((Player) e.getWhoClicked());

                return true;
            });
        }
        else if(loreSlots.contains(e.getRawSlot())){
            previousMove = false;
            e.getWhoClicked().closeInventory();

            Locale.WARP_ICON_NEW_LORE.send(e.getWhoClicked());

            PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                if(!message.equalsIgnoreCase("-cancel")) {
                    itemLore = Arrays.asList(message.split("\\\\n"));
                }

                open(previousMenu);
                PlayerChat.remove((Player) e.getWhoClicked());

                return true;
            });
        }
        else if(confirmSlots.contains(e.getRawSlot())){
            e.getWhoClicked().closeInventory();

            Locale.WARP_ICON_UPDATED.send(e.getWhoClicked());

            islandWarp.setIcon(new ItemBuilder(itemStack).withName(itemName).withLore(itemLore).build());
        }
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(title -> title.replace("{0}", islandWarp.getName()));

        iconSlots.forEach(slot -> inventory.setItem(slot, new ItemBuilder(itemStack)
                .withName(itemName).withLore(itemLore).build()));

        return inventory;
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, islandWarp);
    }

    public static void init(){
        MenuWarpIconEdit menuWarpCategoryIconEdit = new MenuWarpIconEdit(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warp-icon-edit.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warp-icon-edit.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarpCategoryIconEdit, "warp-icon-edit.yml", cfg);

        typeSlots = getSlots(cfg, "icon-type", charSlots);
        renameSlots = getSlots(cfg, "icon-rename", charSlots);
        loreSlots = getSlots(cfg, "icon-relore", charSlots);
        confirmSlots = getSlots(cfg, "icon-confirm", charSlots);
        iconSlots = getSlots(cfg, "icon-slots", charSlots);

        charSlots.delete();

        menuWarpCategoryIconEdit.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, IslandWarp islandWarp){
        new MenuWarpIconEdit(superiorPlayer, islandWarp).open(previousMenu);
    }

}
