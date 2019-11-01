package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class MemberRoleMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";
    private static Map<Integer, PlayerRole> roleSlots = new HashMap<>();

    private SuperiorPlayer targetPlayer;

    private MemberRoleMenu(SuperiorPlayer targetPlayer){
        super("rolePage");
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(!roleSlots.containsKey(e.getRawSlot()))
            return;

        PlayerRole playerRole = roleSlots.get(e.getRawSlot());

        if(playerRole.isLastRole()){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island transfer " + targetPlayer.getName());
        }

        else{
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " " + playerRole.toString());
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title.replace("{}", targetPlayer.getName()));
        inv.setContents(inventory.getContents());
        return inv;
    }

    public static void init(){
        MemberRoleMenu memberRoleMenu = new MemberRoleMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        convertConfig(cfg);

        inventory = FileUtils.loadGUI(memberRoleMenu, cfg.getConfigurationSection("roles-panel"), 5, "");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("roles-panel.title"));

        for(PlayerRole playerRole : plugin.getPlayers().getRoles()){
            if(cfg.contains("roles-panel.roles." + playerRole.toString().toLowerCase())){
                ConfigurationSection roleSection = cfg.getConfigurationSection("roles-panel.roles." + playerRole.toString().toLowerCase());
                ItemStack roleButton = FileUtils.getItemStack(roleSection);
                int roleSlot = roleSection.getInt("slot");
                memberRoleMenu.addSound(roleSlot, FileUtils.getSound(roleSection.getConfigurationSection("sound")));
                memberRoleMenu.addCommands(roleSlot, cfg.getStringList("commands"));

                inventory.setItem(roleSlot, roleButton);
                roleSlots.put(roleSlot, playerRole);
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MemberRoleMenu(targetPlayer).open(superiorPlayer, previousMenu);
    }

    private static void convertConfig(YamlConfiguration cfg){
        if(cfg.contains("roles-panel.member-role"))
            cfg.set("roles-panel.roles.member", cfg.getConfigurationSection("roles-panel.member-role"));

        if(cfg.contains("roles-panel.mod-role"))
            cfg.set("roles-panel.roles.moderator", cfg.getConfigurationSection("roles-panel.mod-role"));

        if(cfg.contains("roles-panel.admin-role"))
            cfg.set("roles-panel.roles.admin", cfg.getConfigurationSection("roles-panel.admin-role"));

        if(cfg.contains("roles-panel.leader-role"))
            cfg.set("roles-panel.roles.leader", cfg.getConfigurationSection("roles-panel.leader-role"));
    }

}
