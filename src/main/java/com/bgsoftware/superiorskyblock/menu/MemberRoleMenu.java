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

public final class MemberRoleMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static ItemStack memberButton, modButton, adminButton, leaderButton;
    private static Sound memberSound, modSound, adminSound, leaderSound;

    private SuperiorPlayer targetPlayer;

    private MemberRoleMenu(SuperiorPlayer targetPlayer){
        super("rolePage");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(memberButton.equals(clickedItem)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " member");
        }

        else if(modButton.equals(clickedItem)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " moderator");
        }

        else if(adminButton.equals(clickedItem)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " admin");
        }

        else if(leaderButton.equals(clickedItem)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island transfer " + targetPlayer.getName());
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), inventory.getTitle().replace("{}", targetPlayer.getName()));
        inv.setContents(inventory.getContents());
        return inv;
    }

    public static void init(){
        MemberRoleMenu memberRoleMenu = new MemberRoleMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(memberRoleMenu, cfg.getConfigurationSection("roles-panel"), 5, "");

        memberButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.member-role"));
        modButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.mod-role"));
        adminButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.admin-role"));
        leaderButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.leader-role"));
        int memberSlot = cfg.getInt("roles-panel.member-role.slot");
        int modSlot = cfg.getInt("roles-panel.mod-role.slot");
        int adminSlot = cfg.getInt("roles-panel.admin-role.slot");
        int leaderSlot = cfg.getInt("roles-panel.leader-role.slot");
        memberSound = getSound(cfg.getString("roles-panel.member-role.sound", ""));
        modSound = getSound(cfg.getString("roles-panel.mod-role.sound", ""));
        adminSound = getSound(cfg.getString("roles-panel.admin-role.sound", ""));
        leaderSound = getSound(cfg.getString("roles-panel.leader-role.sound", ""));

        inventory.setItem(memberSlot, memberButton);
        inventory.setItem(modSlot, modButton);
        inventory.setItem(adminSlot, adminButton);
        inventory.setItem(leaderSlot, leaderButton);
    }

    public static MemberRoleMenu createInventory(SuperiorPlayer targetPlayer){
        return new MemberRoleMenu(targetPlayer);
    }

}
