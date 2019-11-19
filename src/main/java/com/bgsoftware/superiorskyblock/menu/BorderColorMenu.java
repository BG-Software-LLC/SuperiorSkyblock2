package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class BorderColorMenu extends SuperiorMenu {

    private static BorderColorMenu instance;
    private static Inventory inventory = null;
    private static int greenColorSlot, blueColorSlot, redColorSlot;

    private BorderColorMenu(){
        super("borderPage");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        BorderColor borderColor;

        if(e.getRawSlot() == greenColorSlot)
            borderColor = BorderColor.GREEN;
        else if(e.getRawSlot() == blueColorSlot)
            borderColor = BorderColor.BLUE;
        else if(e.getRawSlot() == redColorSlot)
            borderColor = BorderColor.RED;
        else return;

        superiorPlayer.setBorderColor(borderColor);
        plugin.getNMSAdapter().setWorldBorder(superiorPlayer, plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));

        Locale.BORDER_PLAYER_COLOR_UPDATED.send(superiorPlayer, StringUtils.format(borderColor.name()));

        e.getWhoClicked().closeInventory();
        super.onClick(e);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void init(){
        BorderColorMenu islandRatingMenu = new BorderColorMenu();

        islandRatingMenu.resetData();

        File file = new File(plugin.getDataFolder(), "guis/border-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/border-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandRatingMenu, cfg.getConfigurationSection("border-gui"), InventoryType.HOPPER, "&l         Border Color");

        ItemStack greenColorItem = FileUtils.getItemStack(cfg.getConfigurationSection("border-gui.green_color"));
        greenColorSlot = cfg.getInt("border-gui.green_color.slot", 0);
        islandRatingMenu.addSound(greenColorSlot, FileUtils.getSound(cfg.getConfigurationSection("border-gui.green_color.sound")));
        islandRatingMenu.addCommands(greenColorSlot, cfg.getStringList("border-gui.green_color.commands"));
        inventory.setItem(greenColorSlot, greenColorItem);

        ItemStack blueColorItem = FileUtils.getItemStack(cfg.getConfigurationSection("border-gui.blue_color"));
        blueColorSlot = cfg.getInt("border-gui.blue_color.slot", 0);
        islandRatingMenu.addSound(blueColorSlot, FileUtils.getSound(cfg.getConfigurationSection("border-gui.blue_color.sound")));
        islandRatingMenu.addCommands(blueColorSlot, cfg.getStringList("border-gui.blue_color.commands"));
        inventory.setItem(blueColorSlot, blueColorItem);

        ItemStack redColorItem = FileUtils.getItemStack(cfg.getConfigurationSection("border-gui.red_color"));
        redColorSlot = cfg.getInt("border-gui.red_color.slot", 0);
        islandRatingMenu.addSound(redColorSlot, FileUtils.getSound(cfg.getConfigurationSection("border-gui.red_color.sound")));
        islandRatingMenu.addCommands(redColorSlot, cfg.getStringList("border-gui.red_color.commands"));
        inventory.setItem(redColorSlot, redColorItem);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        instance.open(superiorPlayer, previousMenu);
    }

}
