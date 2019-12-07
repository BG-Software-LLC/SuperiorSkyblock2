package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuMemberManage extends SuperiorMenu {

    private static int rolesSlot, banSlot, kickSlot;

    private SuperiorPlayer targetPlayer;

    private MenuMemberManage(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuMemberManage", superiorPlayer);
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == rolesSlot){
            previousMove = false;
            MenuMemberRole.openInventory(SSuperiorPlayer.of(e.getWhoClicked()), this, targetPlayer);
        }

        else if(e.getRawSlot() == banSlot){
            Bukkit.dispatchCommand(e.getWhoClicked(), "island ban " + targetPlayer.getName());
        }

        else if(e.getRawSlot() == kickSlot){
            Bukkit.dispatchCommand(e.getWhoClicked(), "island kick " + targetPlayer.getName());
        }
    }

    @Override
    public Inventory getInventory() {
        return super.buildInventory(title -> title.replace("{}", targetPlayer.getName()));
    }

    public static void init(){
        MenuMemberManage menuMemberManage = new MenuMemberManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/member-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/member-manage.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMemberManage, "member-manage.yml", cfg);

        rolesSlot = charSlots.getOrDefault(cfg.getString("roles", "%").charAt(0), Collections.singletonList(-1)).get(0);
        banSlot = charSlots.getOrDefault(cfg.getString("ban", "*").charAt(0), Collections.singletonList(-1)).get(0);
        kickSlot = charSlots.getOrDefault(cfg.getString("kick", "^").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MenuMemberManage(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer){
        destroyMenus(MenuMemberManage.class, menuMemberManage -> menuMemberManage.targetPlayer.equals(targetPlayer));
    }

}
