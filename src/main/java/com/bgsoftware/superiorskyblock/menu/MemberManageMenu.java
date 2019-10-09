package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class MemberManageMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";
    private static int rolesSlot, banSlot, kickSlot;

    private SuperiorPlayer targetPlayer;

    private MemberManageMenu(SuperiorPlayer targetPlayer){
        super("playerPage");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
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
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title.replace("{}", targetPlayer.getName()));
        inv.setContents(inventory.getContents());
        return inv;
    }

    public static void init(){
        MemberManageMenu memberManageMenu = new MemberManageMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(memberManageMenu, cfg.getConfigurationSection("players-panel"), 6, "");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("players-panel.title"));

        rolesSlot = cfg.getInt("players-panel.roles.slot");
        banSlot = cfg.getInt("players-panel.ban.slot");
        kickSlot = cfg.getInt("players-panel.kick.slot");

        memberManageMenu.addSound(rolesSlot, FileUtils.getSound(cfg.getConfigurationSection("players-panel.roles.sound")));
        memberManageMenu.addSound(banSlot, FileUtils.getSound(cfg.getConfigurationSection("players-panel.ban.sound")));
        memberManageMenu.addSound(kickSlot, FileUtils.getSound(cfg.getConfigurationSection("players-panel.kick.sound")));
        memberManageMenu.addCommands(rolesSlot, cfg.getStringList("players-panel.roles.commands"));
        memberManageMenu.addCommands(banSlot, cfg.getStringList("players-panel.ban.commands"));
        memberManageMenu.addCommands(kickSlot, cfg.getStringList("players-panel.kick.commands"));

        ItemStack rolesButton = FileUtils.getItemStack(cfg.getConfigurationSection("players-panel.roles"));
        ItemStack banButton = FileUtils.getItemStack(cfg.getConfigurationSection("players-panel.ban"));
        ItemStack kickButton = FileUtils.getItemStack(cfg.getConfigurationSection("players-panel.kick"));

        inventory.setItem(rolesSlot, rolesButton);
        inventory.setItem(banSlot, banButton);
        inventory.setItem(kickSlot, kickButton);
    }

    public static MemberManageMenu createInventory(SuperiorPlayer targetPlayer){
        return new MemberManageMenu(targetPlayer);
    }

    public static SuperiorMenu getMenu(SuperiorPlayer targetPlayer){
        return new MemberManageMenu(targetPlayer);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MemberManageMenu(targetPlayer).open(superiorPlayer, previousMenu);
    }

}
