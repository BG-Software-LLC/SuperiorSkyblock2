package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIIdentifier;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;

import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class PanelHandler {

    private SuperiorSkyblockPlugin plugin;
    public GUIInventory playerPage, rolePage;

    public Map<UUID, UUID> islands = new HashMap<>();

    public PanelHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadMenus(plugin);
    }

    private void loadMenus(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        initRolePage(cfg);
    }

    private Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

    public void openRolePanel(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(new GUIIdentifier(GUIInventory.ROLE_PAGE_IDENTIFIER), rolePage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(rolePage.getContents());
        rolePage.openInventory(superiorPlayer, inventory);
    }

    private void initRolePage(YamlConfiguration cfg){
        rolePage = FileUtil.getGUI(GUIInventory.ROLE_PAGE_IDENTIFIER, cfg.getConfigurationSection("roles-panel"), 5, "");

        ItemStack memberButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.member-role"));
        ItemStack modButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.mod-role"));
        ItemStack adminButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.admin-role"));
        ItemStack leaderButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.leader-role"));
        int memberSlot = cfg.getInt("roles-panel.member-role.slot");
        int modSlot = cfg.getInt("roles-panel.mod-role.slot");
        int adminSlot = cfg.getInt("roles-panel.admin-role.slot");
        int leaderSlot = cfg.getInt("roles-panel.leader-role.slot");
        Sound memberSound = getSound(cfg.getString("roles-panel.member-role.sound", ""));
        Sound modSound = getSound(cfg.getString("roles-panel.mod-role.sound", ""));
        Sound adminSound = getSound(cfg.getString("roles-panel.admin-role.sound", ""));
        Sound leaderSound = getSound(cfg.getString("roles-panel.leader-role.sound", ""));

        rolePage.setItem(memberSlot, memberButton);
        rolePage.setItem(modSlot, modButton);
        rolePage.setItem(adminSlot, adminButton);
        rolePage.setItem(leaderSlot, leaderButton);

        rolePage.put("memberSound", memberSound);
        rolePage.put("modSound", modSound);
        rolePage.put("adminSound", adminSound);
        rolePage.put("leaderSound", leaderSound);
        rolePage.put("memberSlot", memberSlot);
        rolePage.put("modSlot", modSlot);
        rolePage.put("adminSlot", adminSlot);
        rolePage.put("leaderSlot", leaderSlot);
    }

}
