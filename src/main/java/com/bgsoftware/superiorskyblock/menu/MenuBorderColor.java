package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuBorderColor extends SuperiorMenu {

    private static int greenColorSlot, blueColorSlot, redColorSlot;

    private MenuBorderColor(SuperiorPlayer superiorPlayer){
        super("menuBorderColor", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
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

        previousMove = false;
        e.getWhoClicked().closeInventory();
    }

    public static void init(){
        MenuBorderColor menuBorderColor = new MenuBorderColor(null);

        File file = new File(plugin.getDataFolder(), "menus/border-color.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/border-color.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuBorderColor, cfg);

        greenColorSlot = charSlots.getOrDefault(cfg.getString("green-color", "@").charAt(0), Collections.singletonList(-1)).get(0);
        redColorSlot = charSlots.getOrDefault(cfg.getString("red-color", "^").charAt(0), Collections.singletonList(-1)).get(0);
        blueColorSlot = charSlots.getOrDefault(cfg.getString("blue-color", "$").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuBorderColor(superiorPlayer).open(previousMenu);
    }

}
