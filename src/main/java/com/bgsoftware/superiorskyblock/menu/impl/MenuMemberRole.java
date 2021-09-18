package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MenuMemberRole extends SuperiorMenu {

    private static final Map<Integer, PlayerRole> roleSlots = new HashMap<>();

    private final SuperiorPlayer targetPlayer;

    private MenuMemberRole(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuMemberRole", superiorPlayer);
        this.targetPlayer = targetPlayer;
        updateTargetPlayer(targetPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(!roleSlots.containsKey(e.getRawSlot()))
            return;

        PlayerRole playerRole = roleSlots.get(e.getRawSlot());

        if(playerRole.isLastRole()){
            plugin.getCommands().dispatchSubCommand(superiorPlayer.asPlayer(), "transfer",
                    targetPlayer.getName());
        }

        else{
            plugin.getCommands().dispatchSubCommand(superiorPlayer.asPlayer(), "setrole",
                    targetPlayer.getName() + " " + playerRole);
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetPlayer);
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> PlaceholderHook.parse(targetPlayer.asOfflinePlayer(), title.replace("{}", targetPlayer.getName())));
    }

    public static void init(){
        MenuMemberRole menuMemberRole = new MenuMemberRole(null, null);

        File file = new File(plugin.getDataFolder(), "menus/member-role.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/member-role.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuMemberRole.resetData();

        menuMemberRole.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menuMemberRole.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menuMemberRole.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menuMemberRole.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

        List<String> pattern = cfg.getStringList("pattern");

        menuMemberRole.setRowsSize(pattern.size());
        int backButton = -1;
        char backButtonChar = cfg.getString("back", " ").charAt(0);

        roleSlots.clear();

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(backButtonChar == ch){
                        backButton = slot;
                    }
                    else if (cfg.contains("items." + ch + ".role")){
                        roleSlots.put(slot, SPlayerRole.of(cfg.getString("items." + ch + ".role")));
                    }

                    menuMemberRole.addFillItem(slot, FileUtils.getItemStack("member-role.yml", cfg.getConfigurationSection("items." + ch)));
                    menuMemberRole.addCommands(slot, cfg.getStringList("commands." + ch));
                    menuMemberRole.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                    String permission = cfg.getString("permissions." + ch + ".permission");
                    SoundWrapper noAccessSound = FileUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));
                    menuMemberRole.addPermission(slot, permission, noAccessSound);

                    slot++;
                }
            }
        }

        menuMemberRole.setBackButton(backButton);

        if(plugin.getSettings().isOnlyBackButton() && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[biomes.yml] Menu doesn't have a back button, it's impossible to close it.");

        menuMemberRole.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MenuMemberRole(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer){
        destroyMenus(MenuMemberRole.class, menuMemberRole -> menuMemberRole.targetPlayer.equals(targetPlayer));
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("roles-panel.title"));

        int size = cfg.getInt("roles-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("roles-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("roles-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("roles-panel.roles")) {
            for (String roleName : cfg.getConfigurationSection("roles-panel.roles").getKeys(false)){
                ConfigurationSection section = cfg.getConfigurationSection("roles-panel.roles." + roleName);
                char itemChar = itemChars[charCounter++];
                section.set("role", StringUtils.format(roleName));
                MenuConverter.convertItem(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
