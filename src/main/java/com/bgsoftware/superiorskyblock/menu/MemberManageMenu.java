package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class MemberManageMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static int rolesSlot, banSlot, kickSlot;
    private static Sound rolesSound, banSound, kickSound;

    private SuperiorPlayer targetPlayer;

    private MemberManageMenu(SuperiorPlayer targetPlayer){
        super("playerPage");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if(e.getRawSlot() == rolesSlot){
            previousMove = false;
            MemberRoleMenu.createInventory(targetPlayer).open(SSuperiorPlayer.of(e.getWhoClicked()), this);
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
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), inventory.getTitle().replace("{}", targetPlayer.getName()));
        inv.setContents(inventory.getContents());
        return inv;
    }

    public static void init(){
        MemberManageMenu memberManageMenu = new MemberManageMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(memberManageMenu, cfg.getConfigurationSection("players-panel"), 6, "");

        rolesSlot = cfg.getInt("players-panel.roles.slot");
        banSlot = cfg.getInt("players-panel.ban.slot");
        kickSlot = cfg.getInt("players-panel.kick.slot");
        rolesSound = getSound(cfg.getString("players-panel.roles.sound", ""));
        banSound = getSound(cfg.getString("players-panel.ban.sound", ""));
        kickSound = getSound(cfg.getString("players-panel.kick.sound", ""));

        ItemStack rolesButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.roles"));
        ItemStack banButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.ban"));
        ItemStack kickButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.kick"));

        inventory.setItem(rolesSlot, rolesButton);
        inventory.setItem(banSlot, banButton);
        inventory.setItem(kickSlot, kickButton);
    }

    public static MemberManageMenu createInventory(SuperiorPlayer targetPlayer){
        return new MemberManageMenu(targetPlayer);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MemberManageMenu(targetPlayer).open(superiorPlayer, previousMenu);
    }

}
