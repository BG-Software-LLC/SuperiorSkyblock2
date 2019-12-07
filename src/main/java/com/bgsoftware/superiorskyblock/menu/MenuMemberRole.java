package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MenuMemberRole extends SuperiorMenu {

    private static Map<Integer, PlayerRole> roleSlots = new HashMap<>();

    private SuperiorPlayer targetPlayer;

    private MenuMemberRole(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuMemberRole", superiorPlayer);
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
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
        return super.buildInventory(title -> title.replace("{}", targetPlayer.getName()));
    }

    public static void init(){
        MenuMemberRole menuMemberRole = new MenuMemberRole(null, null);

        File file = new File(plugin.getDataFolder(), "menus/member-role.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/member-role.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuMemberRole.resetData();

        menuMemberRole.setTitle(ChatColor.translateAlternateColorCodes('&', cfg.getString("title", "")));
        menuMemberRole.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));

        List<String> pattern = cfg.getStringList("pattern");

        menuMemberRole.setRowsSize(pattern.size());

        roleSlots = new HashMap<>();

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(cfg.contains("items." + ch + ".role"))
                        roleSlots.put(slot, SPlayerRole.of(cfg.getString("items." + ch + ".role")));

                    menuMemberRole.addFillItem(slot,  FileUtils.getItemStack(cfg.getConfigurationSection("items." + ch)));
                    menuMemberRole.addCommands(slot, cfg.getStringList("commands." + ch));
                    menuMemberRole.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                    slot++;
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MenuMemberRole(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer){
        destroyMenus(MenuMemberRole.class, menuMemberRole -> menuMemberRole.targetPlayer.equals(targetPlayer));
    }

}
